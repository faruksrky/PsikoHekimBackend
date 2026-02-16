# Proje 3: BPMN (Camunda Zeebe + Elasticsearch)

BPMN iş akışı motoru. Backend `/api/bpmn/patient/start-process` endpoint'i Zeebe'ye mesaj yayınlar.

## Başlatma

```bash
cd PsikoHekimBackend
docker compose -f docker-compose/3-bpmn/docker-compose.yml up -d
```

## Servisler

| Servis | Port | Açıklama |
|--------|------|----------|
| Elasticsearch | 9200 | Zeebe export için |
| Zeebe | 26500 | BPMN iş akışı motoru |

## Backend Bağlantısı

Backend'in Zeebe'ye bağlanması için `zeebe.client.broker.gatewayAddress` ayarlanmalı:

- **Lokal (3-bpmn ile aynı makine):** `localhost:26500`
- **Docker (backend ayrı container):** `zeebe:26500` (network üzerinden)
- **Prod:** `.env` ile `ZEEBE_GATEWAY=zeebe:26500` veya sunucu IP

`application-dev.yml` varsayılan: `localhost:26501` (archive compose portu). 3-bpmn kullanıyorsan `26500` olarak güncelle veya env ile override et.
