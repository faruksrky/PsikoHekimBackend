package com_psikohekim.psikohekim_appt.service;

import com_psikohekim.psikohekim_appt.dto.request.TherapistRequest;
import com_psikohekim.psikohekim_appt.dto.response.TherapistResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistDashboardResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistStatistics;
import com_psikohekim.psikohekim_appt.exception.ConflictException;
import com_psikohekim.psikohekim_appt.exception.InvalidRequestException;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.model.Therapist;

import java.util.List;
import java.util.Map;

/**
 * Danışman (Therapist) yönetimi için service interface
 * - Danışman CRUD işlemleri
 * - Danışman profil yönetimi
 * - Danışman dashboard ve istatistikleri
 */
public interface TherapistService {

     // ========== CORE THERAPIST OPERATIONS ==========

     /**
      * Yeni danışman ekleme
      */
     TherapistResponse addTherapist(TherapistRequest therapistRequest) throws ConflictException, InvalidRequestException;

     /**
      * Tüm danışmanları getirme
      */
     Map<String, List<TherapistResponse>> getTherapists() throws ResourceNotFoundException;

     /**
      * Danışman güncelleme
      */
     TherapistResponse updateTherapist(Long therapistId, TherapistRequest therapistRequest) throws ResourceNotFoundException, ConflictException;

     /**
      * Danışman silme
      */
     void deleteTherapist(Long therapistId) throws ResourceNotFoundException;

     /**
      * ID ile danışman getirme
      */
     TherapistResponse getTherapistById(Long therapistId) throws ResourceNotFoundException;

     /**
      * Email ile danışman bulma
      */
     Therapist findByEmail(String email) throws ResourceNotFoundException;

     // ========== REFERENCE DATA ==========

     /**
      * Psikolog uzmanlık alanları
      */
     List<String> getPyschologistAreas();

     /**
      * Psikiyatrist uzmanlık alanları
      */
     List<String> getPsychiatristAreas();

     // ========== DASHBOARD & STATISTICS ==========

     /**
      * Danışman dashboard verilerini getirme
      */
     TherapistDashboardResponse getTherapistDashboard(Long therapistId) throws ResourceNotFoundException;

     /**
      * Danışman istatistikleri hesaplama
      */
     TherapistStatistics getTherapistStatistics(Long therapistId, String period) throws ResourceNotFoundException;

     // ========== PROFILE MANAGEMENT ==========

     /**
      * Danışman profil fotoğrafı güncelleme
      */
     void updateProfilePhoto(Long therapistId, String photoUrl) throws ResourceNotFoundException;

     /**
      * Danışman şifre güncelleme
      */
     void updatePassword(Long therapistId, String currentPassword, String newPassword) throws ResourceNotFoundException;

     /**
      * Danışman hesabını aktif/pasif yapma
      */
     void toggleAccountStatus(Long therapistId, boolean isActive) throws ResourceNotFoundException;
}