# Sentinel Production Deployment Checklist

## Pre-requisitos
- [ ] VPS contratada (mínimo 8GB RAM, 4 CPU, 100GB SSD)
- [ ] Dominio registrado y DNS apuntando a VPS
- [ ] Acceso SSH a VPS

## Paso 1: Preparar VPS (15-20 min)
```bash
# Conectar a VPS
ssh root@TU_IP_VPS

# Crear usuario
adduser sentinel
usermod -aG sudo sentinel
su - sentinel

# Actualizar sistema
sudo apt update && sudo apt upgrade -y

# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
newgrp docker

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Instalar Nginx
sudo apt install nginx -y
sudo systemctl enable nginx

# Configurar firewall
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

## Paso 2: Configurar Nginx (10 min)
```bash
sudo nano /etc/nginx/sites-available/sentinel
```

Copiar configuración de `vps_deployment_guide.md` (sección 3)

```bash
sudo ln -s /etc/nginx/sites-available/sentinel /etc/nginx/sites-enabled/
sudo rm /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl restart nginx
```

## Paso 3: Clonar Proyecto (5 min)
```bash
mkdir -p ~/sentinel-prod
cd ~/sentinel-prod
git clone https://github.com/TU_USUARIO/sentinel.git
cd sentinel
```

## Paso 4: Configurar Variables de Entorno (5 min)
```bash
# Copiar template
cp .env.production.example .env.production

# Editar y cambiar TODOS los valores
nano .env.production
```

**IMPORTANTE:** Cambiar TODOS los passwords y secrets!

## Paso 5: SSL con Let's Encrypt (5 min)
```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d tudominio.com -d www.tudominio.com
```

## Paso 6: Primer Deploy Manual (20-30 min)
```bash
# Levantar servicios
docker-compose -f docker-compose.prod.yml --env-file .env.production up -d

# Ver logs
docker-compose -f docker-compose.prod.yml logs -f

# Verificar que todos estén running
docker ps
```

## Paso 7: Configurar CI/CD (10 min)

### En VPS - Generar SSH Key
```bash
ssh-keygen -t ed25519 -C "github-actions"
cat ~/.ssh/id_ed25519.pub >> ~/.ssh/authorized_keys
cat ~/.ssh/id_ed25519  # Copiar esto
```

### En GitHub
1. Repo → Settings → Secrets and variables → Actions
2. Agregar secrets:
   - `VPS_HOST`: Tu IP de VPS
   - `VPS_USER`: sentinel
   - `SSH_PRIVATE_KEY`: Pegar la key privada

## Paso 8: Verificar Deploy (10 min)
- [ ] Abrir https://tudominio.com → Frontend carga
- [ ] Login funciona
- [ ] Crear workspace
- [ ] Ver logs: `docker-compose -f docker-compose.prod.yml logs -f`

## Paso 9: Prueba de CI/CD
```bash
# En tu máquina local
git add .
git commit -m "Test CI/CD"
git push origin main

# Esperar 2-3 min, ir a GitHub → Actions
# Ver que el workflow se ejecuta
# Verificar que el sitio se actualiza
```

## Paso 10: Monitoreo (Opcional - 15 min)
Seguir guía en `vps_deployment_guide.md` sección 7 para Grafana/Prometheus

---

## Troubleshooting Común

### Servicios no inician
```bash
docker-compose -f docker-compose.prod.yml logs [nombre-servicio]
```

### Nginx error
```bash
sudo nginx -t
sudo tail -f /var/log/nginx/error.log
```

### SSL no funciona
```bash
sudo certbot certificates
sudo certbot renew --dry-run
```

### Puerto ocupado
```bash
sudo lsof -i :puerto
sudo kill -9 PID
```

---

## Tiempo Total Estimado
- **Primera vez:** 2-3 horas
- **Actualizaciones futuras:** Automáticas (1-2 min vía CI/CD)
