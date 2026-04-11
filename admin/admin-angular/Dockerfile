# ============================
# Stage 1: Build com Node
# ============================
FROM node:22-alpine AS build

WORKDIR /app

# Cache de dependências
COPY package.json package-lock.json ./
RUN npm ci

# Copia código e compila
# ┌──────────────────────────────────────────────────────────────┐
# │ 🔧 DESENVOLVIMENTO — build sem minificação + source maps   │
# │    Permite debug no browser F12 → Sources → seus .ts        │
# │                                                              │
# │ ⚠️  PRODUÇÃO — trocar por:                                  │
# │    RUN npx ng build --configuration production               │
# │    E no angular.json remover "sourceMap": true do production │
# └──────────────────────────────────────────────────────────────┘
COPY . .
RUN npx ng build --configuration development

# ============================
# Stage 2: Servir com Nginx
# ============================
FROM nginx:alpine

# Remove config padrão do nginx
RUN rm /etc/nginx/conf.d/default.conf

# Copia config customizada
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copia build do Angular
COPY --from=build /app/dist/admin-angular/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

