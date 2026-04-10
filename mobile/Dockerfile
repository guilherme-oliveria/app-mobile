# ============================
# Flutter APK Builder
# ============================
# Gera os APKs do motoboy_app e store_app
# Os APKs ficam em /output/ dentro do container
#
# Uso:
#   docker compose run --rm flutter-builder
#   Os APKs são copiados para ./mobile/apk-output/ no host

FROM ghcr.io/cirruslabs/flutter:3.29.3 AS builder

# ── Motoboy App ──────────────────────────────────────
WORKDIR /apps/motoboy_app

COPY motoboy_app/pubspec.yaml motoboy_app/pubspec.lock* ./
RUN flutter pub get

COPY motoboy_app/ .
RUN flutter create --project-name motoboy_app --org com.delivery . \
    && flutter build apk --release

# ── Store App ────────────────────────────────────────
WORKDIR /apps/store_app

COPY store_app/pubspec.yaml store_app/pubspec.lock* ./
RUN flutter pub get

COPY store_app/ .
RUN flutter create --project-name store_app --org com.delivery . \
    && flutter build apk --release

# ── Copiar APKs para pasta de output ────────────────
FROM alpine:3.20

RUN mkdir -p /output

COPY --from=builder /apps/motoboy_app/build/app/outputs/flutter-apk/app-release.apk /output/motoboy-app.apk
COPY --from=builder /apps/store_app/build/app/outputs/flutter-apk/app-release.apk /output/store-app.apk

CMD ["echo", "APKs gerados em /output/. Use: docker cp <container>:/output/ ."]

