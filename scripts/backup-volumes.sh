#!/bin/bash
# Backup automático de volúmenes críticos de Sentinel
# Ejecutar diariamente vía cron: 0 2 * * * /opt/sentinel/scripts/backup-volumes.sh

BACKUP_DIR="/opt/sentinel/backups"
DATE=$(date +%Y%m%d_%H%M%S)
LOG_FILE="/var/log/sentinel-backup.log"

# Crear directorio de backup
mkdir -p $BACKUP_DIR

echo "[$DATE] 🔄 Iniciando backup de volúmenes..." | tee -a $LOG_FILE

# Backup de n8n (workflows y configuración)
echo "[$DATE] Backing up n8n..." | tee -a $LOG_FILE
docker run --rm \
  -v sentinel-deployment_n8n_data:/data:ro \
  -v $BACKUP_DIR:/backup \
  alpine tar czf /backup/n8n_$DATE.tar.gz -C /data . 2>&1 | tee -a $LOG_FILE

# Backup de PostgreSQL (usuarios, tenants, proyectos, billing)
echo "[$DATE] Backing up PostgreSQL..." | tee -a $LOG_FILE
docker exec sentinel-postgres pg_dumpall -U sentinel | gzip > $BACKUP_DIR/postgres_$DATE.sql.gz 2>&1 | tee -a $LOG_FILE

# Backup de MongoDB (resultados de scans)
echo "[$DATE] Backing up MongoDB..." | tee -a $LOG_FILE
docker exec sentinel-mongodb mongodump \
  --username=sentinel \
  --password=sentinel123 \
  --authenticationDatabase=admin \
  --archive=/tmp/mongodb_$DATE.archive 2>&1 | tee -a $LOG_FILE

docker cp sentinel-mongodb:/tmp/mongodb_$DATE.archive $BACKUP_DIR/ 2>&1 | tee -a $LOG_FILE
docker exec sentinel-mongodb rm /tmp/mongodb_$DATE.archive

# Backup de Kong (configuración del gateway)
echo "[$DATE] Backing up Kong..." | tee -a $LOG_FILE
docker exec sentinel-kong-db pg_dump -U kong kong | gzip > $BACKUP_DIR/kong_$DATE.sql.gz 2>&1 | tee -a $LOG_FILE

# Limpiar backups antiguos (mantener últimos 7 días)
echo "[$DATE] Limpiando backups antiguos..." | tee -a $LOG_FILE
find $BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete
find $BACKUP_DIR -name "*.archive" -mtime +7 -delete

# Calcular tamaño total de backups
BACKUP_SIZE=$(du -sh $BACKUP_DIR | cut -f1)

echo "[$DATE] ✅ Backup completado" | tee -a $LOG_FILE
echo "[$DATE] Ubicación: $BACKUP_DIR" | tee -a $LOG_FILE
echo "[$DATE] Tamaño total: $BACKUP_SIZE" | tee -a $LOG_FILE
echo "[$DATE] Archivos creados:" | tee -a $LOG_FILE
ls -lh $BACKUP_DIR/*_$DATE.* 2>&1 | tee -a $LOG_FILE
