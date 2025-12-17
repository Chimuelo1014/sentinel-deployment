# Sentinel VPS Deployment - Quick Start Guide

## 🚀 Deploy en 3 Pasos

### Paso 1: Preparar VPS (Una sola vez)

```bash
# SSH a tu VPS
ssh root@TU_IP_VPS

# Crear usuario sentinel
adduser sentinel
usermod -aG sudo sentinel
su - sentinel

# Ejecutar script de setup
cd ~
git clone https://github.com/Chimuelo1014/sentinel-deployment.git
cd sentinel-deployment
chmod +x vps-setup.sh
./vps-setup.sh
```

### Paso 2: Configurar Credenciales

```bash
# Editar archivo de variables
nano ~/sentinel-deployment/.env.production
```

**IMPORTANTE:** Cambia TODOS los passwords y secrets con valores seguros.

### Paso 3: Obtener SSL y Deploy

```bash
# Obtener certificados SSL
sudo certbot --nginx -d sentinel.crudzaso.com -d service.sentinel.crudzaso.com

# Levantar aplicación
cd ~/sentinel-deployment
docker-compose -f docker-compose.prod.yml --env-file .env.production up -d

# Ver logs
docker-compose -f docker-compose.prod.yml logs -f
```

---

## 🔐 Configurar GitHub Secrets

Para CI/CD automático, ve a tu repo en GitHub:

`Settings → Secrets and variables → Actions → New repository secret`

Agregar:

| Secret Name | Valor |
|------------|-------|
| `VPS_HOST` | IP de tu VPS |
| `VPS_USER` | `sentinel` |
| `SSH_PRIVATE_KEY` | Tu llave privada SSH completa |

### Generar SSH Key para CI/CD:

```bash
# En tu máquina LOCAL
ssh-keygen -t ed25519 -C "github-actions-sentinel" -f ~/.ssh/sentinel_ci

# Copiar PÚBLICA a VPS
ssh-copy-id -i ~/.ssh/sentinel_ci.pub sentinel@TU_IP_VPS

# Copiar PRIVADA a GitHub Secret
cat ~/.ssh/sentinel_ci
# Copiar TODO el output y pegarlo en GitHub Secret SSH_PRIVATE_KEY
```

---

## 🎯 Workflow de Desarrollo

### Hacer Cambios

```bash
# En local
cd C:\Users\USUARIO\Downloads\sentinel_v2\sentinel

# Hacer cambios en el código...

# Commit y push
git add .
git commit -m "feat: nueva funcionalidad"
git push origin main
```

### Deploy Automático

GitHub Actions detecta el push y despliega automáticamente en 2-3 minutos.

Ver progreso: https://github.com/Chimuelo1014/sentinel-deployment/actions

---

## 🌐 URLs de Producción

- **Frontend:** https://sentinel.crudzaso.com
- **Backend API:** https://service.sentinel.crudzaso.com

---

## 🔍 Comandos Útiles

### Ver logs de servicios:
```bash
ssh sentinel@TU_IP_VPS
cd ~/sentinel-deployment
docker-compose -f docker-compose.prod.yml logs -f [service-name]
```

### Reiniciar un servicio:
```bash
docker-compose -f docker-compose.prod.yml restart [service-name]
```

### Ver estado de contenedores:
```bash
docker ps --filter "name=sentinel"
```

### Actualizar manualmente:
```bash
cd ~/sentinel-deployment
git pull origin main
docker-compose -f docker-compose.prod.yml --env-file .env.production up -d --build
```

---

## ⚠️ Troubleshooting

### Nginx no inicia:
```bash
sudo nginx -t
sudo tail -f /var/log/nginx/error.log
```

### SSL no funciona:
```bash
sudo certbot certificates
sudo certbot renew --dry-run
```

### Servicios no arrancan:
```bash
docker-compose -f docker-compose.prod.yml logs [service-name]
```

---

## 📋 Checklist Completo

### Setup Inicial (Una vez)
- [ ] VPS preparada con Docker, Nginx instalados
- [ ] Usuario `sentinel` creado
- [ ] `.env.production` configurado con credenciales reales
- [ ] Certificados SSL obtenidos
- [ ] Primera deployment manual exitosa
- [ ] GitHub Secrets configurados para CI/CD

### Desarrollo Continuo
- [ ] Hacer cambios en local
- [ ] `git push origin main`
- [ ] Verificar deployment en GitHub Actions
- [ ] Probar en https://sentinel.crudzaso.com

---

## 🎉 ¡Listo!

Una vez completado el setup, cada `git push` despliega automáticamente en producción. 🚀
