package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.dto.response.FinanceMonthlySummaryResponse;
import com_psikohekim.psikohekim_appt.dto.response.SessionResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistMonthlyExpenseResponse;
import com_psikohekim.psikohekim_appt.mapper.TherapySessionMapper;
import com_psikohekim.psikohekim_appt.model.ClientSessionPrice;
import com_psikohekim.psikohekim_appt.model.ConsultantEarning;
import com_psikohekim.psikohekim_appt.model.Therapist;
import com_psikohekim.psikohekim_appt.model.TherapySession;
import com_psikohekim.psikohekim_appt.repository.ClientSessionPriceRepository;
import com_psikohekim.psikohekim_appt.repository.ConsultantEarningRepository;
import com_psikohekim.psikohekim_appt.repository.TherapySessionRepository;
import com_psikohekim.psikohekim_appt.service.FinanceService;
import com_psikohekim.psikohekim_appt.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinanceServiceImpl implements FinanceService {

    private final TherapySessionRepository therapySessionRepository;
    private final ConsultantEarningRepository consultantEarningRepository;
    private final ClientSessionPriceRepository clientSessionPriceRepository;
    private final PricingService pricingService;
    private final TherapySessionMapper sessionMapper;

    @Override
    public FinanceMonthlySummaryResponse getMonthlySummary(int year, int month) {
        List<TherapySession> sessions = therapySessionRepository.findCompletedSessionsByMonthYear(month, year);
        for (TherapySession s : sessions) {
            pricingService.ensureSplitPricing(s);
        }

        List<Long> sessionIds = sessions.stream()
                .map(TherapySession::getTherapySessionId)
                .collect(Collectors.toList());

        Map<Long, ClientSessionPrice> clientPriceBySession = sessionIds.isEmpty()
                ? Map.of()
                : clientSessionPriceRepository.findBySessionIdIn(sessionIds).stream()
                        .collect(Collectors.toMap(ClientSessionPrice::getSessionId, p -> p));

        Map<Long, ConsultantEarning> earningBySession = sessionIds.isEmpty()
                ? Map.of()
                : consultantEarningRepository.findBySessionIdIn(sessionIds).stream()
                        .collect(Collectors.toMap(ConsultantEarning::getSessionId, e -> e));

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalPending = BigDecimal.ZERO;

        Map<Long, TherapistSummary> therapistMap = new HashMap<>();

        for (TherapySession session : sessions) {
            BigDecimal sessionFee = session.getSessionFee() != null ? session.getSessionFee() : BigDecimal.ZERO;
            ClientSessionPrice clientPrice = clientPriceBySession.get(session.getTherapySessionId());
            ConsultantEarning earning = earningBySession.get(session.getTherapySessionId());

            // Ciro: Danışan ödemesi (ClientSessionPrice.sessionPrice)
            BigDecimal income = clientPrice != null && clientPrice.getSessionPrice() != null
                    ? clientPrice.getSessionPrice()
                    : sessionFee;

            BigDecimal consultantFee;
            String payoutStatus;
            if (earning != null) {
                consultantFee = earning.getConsultantFee() != null ? earning.getConsultantFee() : sessionFee;
                payoutStatus = earning.getPayoutStatus() != null ? earning.getPayoutStatus() : "PENDING";
            } else {
                consultantFee = sessionFee;
                payoutStatus = "PENDING";
            }

            totalIncome = totalIncome.add(income);
            totalExpense = totalExpense.add(consultantFee);

            if ("PAID".equalsIgnoreCase(payoutStatus)) {
                totalPaid = totalPaid.add(consultantFee);
            } else {
                totalPending = totalPending.add(consultantFee);
            }

            Long therapistId = session.getTherapistId();
            Therapist therapist = session.getTherapist();
            String therapistName = therapist != null
                    ? therapist.getTherapistFirstName() + " " + therapist.getTherapistLastName()
                    : "Bilinmeyen Danışman";

            therapistMap.computeIfAbsent(therapistId, k -> new TherapistSummary(therapistId, therapistName));

            TherapistSummary ts = therapistMap.get(therapistId);
            ts.sessionCount++;
            ts.totalEarning = ts.totalEarning.add(consultantFee);
            if ("PAID".equalsIgnoreCase(payoutStatus)) {
                ts.paidAmount = ts.paidAmount.add(consultantFee);
            } else {
                ts.pendingAmount = ts.pendingAmount.add(consultantFee);
            }
        }

        List<TherapistMonthlyExpenseResponse> therapistExpenses = therapistMap.values().stream()
                .map(ts -> {
                    String status;
                    if (ts.pendingAmount.compareTo(BigDecimal.ZERO) == 0) {
                        status = "FULLY_PAID";
                    } else if (ts.paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                        status = "PARTIAL";
                    } else {
                        status = "PENDING";
                    }
                    return TherapistMonthlyExpenseResponse.builder()
                            .therapistId(ts.therapistId)
                            .therapistName(ts.therapistName)
                            .sessionCount(ts.sessionCount)
                            .totalEarning(ts.totalEarning)
                            .paidAmount(ts.paidAmount)
                            .pendingAmount(ts.pendingAmount)
                            .status(status)
                            .build();
                })
                .sorted(Comparator.comparing(TherapistMonthlyExpenseResponse::getTherapistName))
                .collect(Collectors.toList());

        List<SessionResponse> sessionResponses = sessionMapper.toResponseDtoList(sessions);
        for (SessionResponse sr : sessionResponses) {
            ClientSessionPrice csp = clientPriceBySession.get(sr.getSessionId());
            if (csp != null && csp.getSessionPrice() != null) {
                sr.setClientPrice(csp.getSessionPrice());
            }
        }

        return FinanceMonthlySummaryResponse.builder()
                .year(year)
                .month(month)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .totalPaid(totalPaid)
                .totalPending(totalPending)
                .totalProfit(totalIncome.subtract(totalExpense))
                .therapistExpenses(therapistExpenses)
                .sessions(sessionResponses)
                .build();
    }

    @Override
    @Transactional
    public void markTherapistPaid(Long therapistId, int year, int month) {
        List<TherapySession> sessions = therapySessionRepository.findCompletedSessionsByMonthYear(month, year);
        List<Long> sessionIds = sessions.stream()
                .filter(s -> therapistId.equals(s.getTherapistId()))
                .map(TherapySession::getTherapySessionId)
                .collect(Collectors.toList());

        if (sessionIds.isEmpty()) {
            log.warn("No completed sessions for therapist {} in {}/{}", therapistId, year, month);
            return;
        }

        List<ConsultantEarning> earnings = consultantEarningRepository.findBySessionIdIn(sessionIds);
        LocalDateTime now = LocalDateTime.now();

        for (ConsultantEarning earning : earnings) {
            if (!"PAID".equalsIgnoreCase(earning.getPayoutStatus())) {
                earning.setPayoutStatus("PAID");
                earning.setPaidAt(now);
                consultantEarningRepository.save(earning);
            }
        }

        log.info("Marked {} earnings as PAID for therapist {} in {}/{}", earnings.size(), therapistId, year, month);
    }

    private static class TherapistSummary {
        final Long therapistId;
        final String therapistName;
        int sessionCount = 0;
        BigDecimal totalEarning = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.ZERO;
        BigDecimal pendingAmount = BigDecimal.ZERO;

        TherapistSummary(Long therapistId, String therapistName) {
            this.therapistId = therapistId;
            this.therapistName = therapistName;
        }
    }
}
