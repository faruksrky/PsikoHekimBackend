-- Gelen kutusu / admin onay ayrımı için (TherapistAssignment.adminApprovalOnly)
-- Sunucuda bir kez çalıştırın; ardından backend'i yeniden başlatın.
-- Hata: column "admin_approval_only" does not exist → bu script gerekir.
--
-- Docker'da kullanıcı çoğunlukla "postgres" (UserPsiko değil). Örnek:
--   docker exec -i psikohekim-postgres psql -U postgres -d psikohekim < scripts/add-admin-approval-only-column.sql
-- Kendi .env içindeki POSTGRES_USER farklıysa onu kullanın: grep POSTGRES .env

ALTER TABLE therapist_assignment
    ADD COLUMN IF NOT EXISTS admin_approval_only BOOLEAN NOT NULL DEFAULT FALSE;

-- Eski satırlarda NULL kaldıysa (nadiren):
UPDATE therapist_assignment SET admin_approval_only = FALSE WHERE admin_approval_only IS NULL;
