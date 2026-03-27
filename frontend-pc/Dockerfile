# ---- Build Stage ----
FROM node:18-alpine AS builder

WORKDIR /build

# Cache dependencies
COPY package.json ./
RUN npm install --legacy-peer-deps

# Build application
COPY . .
RUN npm run build

# ---- Runtime Stage ----
FROM nginx:1.25-alpine

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy built files
COPY --from=builder /build/dist /usr/share/nginx/html

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]
