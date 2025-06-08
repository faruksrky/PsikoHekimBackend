package com_psikohekim.psikohekim_appt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TherapistStatistics {
    
    // Hasta istatistikleri
    private Integer totalActivePatients;
    private Integer totalPatientsAllTime;
    private Integer newPatientsThisMonth;
    private Integer completedTreatments;
    
    // Randevu istatistikleri
    private Integer todayAppointmentCount;
    private Integer weeklyAppointmentCount;
    private Integer monthlyAppointmentCount;
    private Integer totalAppointments;
    private Integer completedAppointments;
    private Integer cancelledAppointments;
    private Integer noShowAppointments;
    
    // Performans metrikleri
    private Double attendanceRate; // Katılım oranı
    private Double completionRate; // Tedavi tamamlama oranı
    private Double averageSessionsPerPatient;
    private Double patientSatisfactionScore;
    
    // Finansal istatistikler
    private BigDecimal weeklyRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal yearlyRevenue;
    private BigDecimal totalEarnings;
    private BigDecimal pendingPaymentAmount;
    private BigDecimal averageSessionFee;
    
    // Zaman analizi
    private Double averageSessionDuration; // dakika
    private Integer totalWorkingHours;
    private Integer averageWorkingHoursPerWeek;
    
    // Trend verileri (önceki dönemle karşılaştırma)
    private TrendData patientsTrend;
    private TrendData revenueTrend;
    private TrendData appointmentsTrend;
    
    // Son güncelleme
    private LocalDateTime lastUpdated;
    private String calculationPeriod; // DAILY, WEEKLY, MONTHLY, YEARLY
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private BigDecimal currentValue;
        private BigDecimal previousValue;
        private BigDecimal changeAmount;
        private Double changePercentage;
        private String trendDirection; // UP, DOWN, STABLE
        private String trendColor; // Frontend için renk
    }
} 