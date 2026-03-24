-- Gelen kutusu / admin onay ayrımı için (TherapistAssignment.adminApprovalOnly)
-- Sunucuda bir kez çalıştırın; ardından backend'i yeniden başlatın.
-- Hata: column "admin_approval_only" does not exist → bu script gerekir.

ALTER TABLE therapist_assignment
    ADD COLUMN IF NOT EXISTS admin_approval_only BOOLEAN NOT NULL DEFAULT FALSE;

-- Eski satırlarda NULL kaldıysa (nadiren):
UPDATE therapist_assignment SET admin_approval_only = FALSE WHERE admin_approval_only IS NULL;
