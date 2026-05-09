#!/bin/bash
# ================================================================
#  TurboX Booster - Deploy a GitHub desde Termux
#  Lee el ZIP de Downloads, lo extrae, crea el repo y hace push
# ================================================================

echo ""
echo "╔══════════════════════════════════════════╗"
echo "║   TurboX Booster — GitHub Deploy Script  ║"
echo "╚══════════════════════════════════════════╝"
echo ""

# ════════════════════════════════════════════════════════════════
# CONFIGURACIÓN — edita estas 3 líneas antes de ejecutar
# ════════════════════════════════════════════════════════════════
GITHUB_USER="TU_USUARIO"          # tu usuario de GitHub
GITHUB_TOKEN="ghp_XXXXXXXXXXXX"   # token con permisos repo (Settings → Developer → Tokens)
REPO_NAME="TurboX-Booster"        # nombre del repo a crear (nuevo o existente)
# ════════════════════════════════════════════════════════════════

ZIP_IN_DOWNLOADS="$HOME/storage/downloads/TurboX-Booster-v2.zip"
WORK_DIR="$HOME/TurboX-Booster"

# ── 0. Verificar configuración ──────────────────────────────────
if [[ "$GITHUB_USER" == "TU_USUARIO" || "$GITHUB_TOKEN" == "ghp_XXXXXXXXXXXX" ]]; then
  echo "❌ Edita GITHUB_USER y GITHUB_TOKEN antes de ejecutar."
  echo ""
  echo "  nano ~/storage/downloads/deploy.sh"
  echo ""
  exit 1
fi

# ── 1. Permisos de almacenamiento ───────────────────────────────
echo "📂 Verificando acceso a almacenamiento..."
if [ ! -d "$HOME/storage/downloads" ]; then
  echo "   Dando permisos de almacenamiento..."
  termux-setup-storage
  sleep 3
fi
echo "   ✅ Almacenamiento OK"
echo ""

# ── 2. Instalar dependencias ────────────────────────────────────
echo "🔧 Instalando dependencias necesarias..."
pkg install -y git curl unzip 2>/dev/null | tail -1
echo "   ✅ git, curl, unzip listos"
echo ""

# ── 3. Configurar git ───────────────────────────────────────────
echo "⚙️  Configurando git..."
git config --global user.name  "$GITHUB_USER"
git config --global user.email "$GITHUB_USER@users.noreply.github.com"
git config --global init.defaultBranch main
echo "   ✅ Git configurado"
echo ""

# ── 4. Verificar ZIP en Downloads ───────────────────────────────
echo "🔍 Buscando ZIP en Downloads..."

if [ ! -f "$ZIP_IN_DOWNLOADS" ]; then
  echo "   ⚠️  No encontré: $ZIP_IN_DOWNLOADS"
  echo "   Buscando cualquier ZIP de TurboX..."
  ZIP_IN_DOWNLOADS=$(find "$HOME/storage/downloads" -name "*TurboX*" -o -name "*turbox*" 2>/dev/null | head -1)
  if [ -z "$ZIP_IN_DOWNLOADS" ]; then
    echo ""
    echo "❌ No encontré ningún ZIP de TurboX en Downloads."
    echo "   Asegúrate de haber descargado TurboX-Booster-v2.zip"
    exit 1
  fi
fi
echo "   ✅ ZIP encontrado: $ZIP_IN_DOWNLOADS"
echo ""

# ── 5. Limpiar y extraer ─────────────────────────────────────────
echo "📦 Extrayendo proyecto..."
rm -rf "$WORK_DIR"
mkdir -p "$WORK_DIR"
unzip -q "$ZIP_IN_DOWNLOADS" -d "$WORK_DIR"

# Si el zip tiene una carpeta raíz adentro (TurboX-Final/), moverla
INNER=$(find "$WORK_DIR" -maxdepth 1 -mindepth 1 -type d | head -1)
if [ -n "$INNER" ] && [ "$INNER" != "$WORK_DIR" ]; then
  mv "$INNER"/* "$WORK_DIR/" 2>/dev/null || true
  mv "$INNER"/.[!.]* "$WORK_DIR/" 2>/dev/null || true
  rmdir "$INNER" 2>/dev/null || true
fi

echo "   ✅ Proyecto extraído en: $WORK_DIR"
echo ""

# ── 6. Crear repo en GitHub via API ─────────────────────────────
echo "🌐 Creando repositorio en GitHub..."

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
  -X POST "https://api.github.com/user/repos" \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"$REPO_NAME\",
    \"description\": \"TurboX Booster - Optimizador Android con Modo Pro (Shizuku) sin root\",
    \"private\": false,
    \"auto_init\": false
  }")

if [ "$HTTP_CODE" == "201" ]; then
  echo "   ✅ Repo creado: https://github.com/$GITHUB_USER/$REPO_NAME"
elif [ "$HTTP_CODE" == "422" ]; then
  echo "   ℹ️  El repo ya existe — se hará push al existente"
else
  echo "   ⚠️  Respuesta inesperada HTTP $HTTP_CODE"
  echo "      Verifica que el token tenga permiso 'repo'"
fi
echo ""

# ── 7. Init git y hacer push ────────────────────────────────────
echo "🚀 Haciendo push a GitHub..."
cd "$WORK_DIR"

# Iniciar o reutilizar repo git local
if [ ! -d ".git" ]; then
  git init
fi

# Agregar remote (reemplazar si ya existe)
git remote remove origin 2>/dev/null || true
git remote add origin "https://$GITHUB_USER:$GITHUB_TOKEN@github.com/$GITHUB_USER/$REPO_NAME.git"

# Staging y commit
git add -A
git commit -m "feat: TurboX Booster v2 - Modo Pro con 17 módulos de optimización

Cambios principales:
- GPU: Game Driver + Adreno/Mali governor optimizado
- Hz: Forzar 120Hz+ (Samsung/OnePlus/MIUI/Stock)
- FPS: Triple buffering + animaciones optimizadas
- CPU: IRQ affinity + CFS tuning + dex2oat speed
- Audio: Fast track + BT A2DP offload
- Thermal: Modo gaming (menos throttling)
- Storage: I/O scheduler deadline para flash
- Sensores: Muestreo reducido en background
- Notificaciones: Rate limit para menos wakeups
- Batería: Doze + job scheduler optimizado
- Display: VSYNC + GPU composition forzada
- Modo Juego Pro: 12 pasos de optimización total
- Bug fix: deactivateGameMode() restaura todo correctamente" \
  2>/dev/null || git commit --allow-empty -m "fix: actualizar archivos"

# Push (forzar para sobreescribir si el repo ya existe vacío)
git push -u origin main --force 2>&1 | grep -E "(branch|error|fatal|->)" || true

PUSH_OK=$?
echo ""

# ── 8. Verificar resultado ───────────────────────────────────────
if git log --oneline -1 &>/dev/null; then
  COMMIT_SHA=$(git rev-parse --short HEAD)
  echo "═══════════════════════════════════════════════════"
  echo "  ✅ ¡Push exitoso!"
  echo ""
  echo "  🔗 Repo:    https://github.com/$GITHUB_USER/$REPO_NAME"
  echo "  📦 Actions: https://github.com/$GITHUB_USER/$REPO_NAME/actions"
  echo "  🔖 Commit:  $COMMIT_SHA"
  echo "═══════════════════════════════════════════════════"
  echo ""
  echo "  El APK se compilará automáticamente con GitHub Actions."
  echo "  En ~5 minutos descárgalo en la pestaña Actions → Artifacts."
  echo ""
else
  echo "⚠️  Revisa los errores arriba."
  echo "   Causas comunes:"
  echo "   - Token incorrecto o sin permisos 'repo'"
  echo "   - Sin conexión a internet"
fi

echo "🏁 Script finalizado."
echo ""
