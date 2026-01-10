4단계 진행하겠습니다! 무료 도메인 + Nginx + HTTPS 완벽 가이드입니다.

## 📋 Step 1: 무료 도메인 구입 (5분)

**추천: `freedns.afraid.org` 또는 `duckdns.org` (무료 + 동적 DNS)**

### DuckDNS 방식 (가장 간단)
```bash
# 1. https://www.duckdns.org 가입 (GitHub로 로그인 가능)
# 2. "Add Domain" → 도메인명 입력 (예: recipemate)
# 3. current ip에 13.125.48.36 입력 & 클릭
# → recipemate.duckdns.org 생성 완료!

# 4. EC2에서 동적 업데이트 스크립트 (IP 변경 시 자동 갱신)
mkdir -p ~/duckdns
cat > ~/duckdns/duck.sh << 'EOF'
#!/bin/bash
CURRENT_IP=$(curl -s ifconfig.me)
URL="https://www.duckdns.org/update?domains=recipemate&token=32343a7d-85f2-474d-bbc1-0a9e0d3f8bd4&ip=$CURRENT_IP"
echo "$URL"
curl -s "$URL" -o ~/duckdns/duck.log
echo "$(date): Updated to $CURRENT_IP - $(cat ~/duckdns/duck.log)" >> ~/duckdns/duck.log
EOF

chmod +x ~/duckdns/duck.sh
(crontab -l 2>/dev/null; echo "*/30 * * * * ~/duckdns/duck.sh") | crontab -

# 5. 테스트
~/duckdns/duck.sh
cat ~/duckdns/duck.log  # "OK" 확인
curl http://recipemate.duckdns.org:8080 # 현재 앱 접속 확인
```

***

## 🔧 Step 2: Nginx 설정 & docker-compose.yml 수정

### 1. 로컬에서 Nginx 설정 파일 생성 (프로젝트 루트)

- 기존 nginx.conf, conf.d/recipemate.conf 파일에서 조금만 수정

### 2. docker-compose.yml 수정 (Nginx 주석 해제)

### 3. Git에 업로드 & EC2 반영

```bash
# 로컬에서
git add .
git commit -m "feat: Add Nginx reverse proxy for 2단계"
git push origin main

# EC2에서
git pull
```

***

## 🔐 Step 3: HTTPS (SSL) 설정 with Certbot

```bash
# 1. Certbot Docker 이미지로 인증서 발급
docker run -it --rm --name certbot \
  -v /home/ec2-user/recipemate/nginx/ssl:/etc/letsencrypt \
  certbot/certbot certonly --standalone \
  -d recipemate.duckdns.org \
  --email your-email@gmail.com \
  --agree-tos --non-interactive

# 2. 인증서 확인
ls -la ~/recipemate/nginx/ssl/live/recipemate.duckdns.org/

# 3. Nginx HTTPS 설정 추가
cat > nginx/conf.d/recipemate.conf << 'EOF'
# HTTP → HTTPS 리다이렉트
server {
    listen 80;
    server_name recipemate.duckdns.org;
    return 301 https://$server_name$request_uri;
}

# HTTPS 서버
server {
    listen 443 ssl http2;
    server_name recipemate.duckdns.org;
    client_max_body_size 50M;

    ssl_certificate /etc/nginx/ssl/live/recipemate.duckdns.org/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/live/recipemate.duckdns.org/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    location / {
        proxy_pass http://app:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
EOF

# 4. Docker Compose 재시작
docker compose down
docker compose up -d --build --force-recreate

# 5. 인증서 자동 갱신 (3개월마다)
(crontab -l 2>/dev/null; echo "0 0 1 * * docker run --rm -v ~/recipemate/nginx/ssl:/etc/letsencrypt certbot/certbot renew") | crontab -
```

***

## 🔒 Step 4: AWS 보안 그룹 수정

**목표:** 8080 차단, 80/443만 공개

```bash
# AWS Console에서
# Security Group: launch-wizard-1
# Inbound Rules 수정:
# ❌ 8080 (Custom TCP) 삭제
# ✅ 22 (SSH) - My IP 유지
# ✅ 80 (HTTP) - 0.0.0.0/0 유지
# ✅ 443 (HTTPS) - 0.0.0.0/0 추가
```

***

## ✅ 최종 확인

```bash
# 1. HTTPS 접속
https://recipemate.duckdns.org

# 2. 헬스체크
curl -v https://recipemate.duckdns.org/actuator/health

# 3. 로그 확인
docker compose logs -f nginx
docker compose logs -f app
```

**완료! 이제 `https://recipemate.duckdns.org`로 안전하게 접속 가능합니다.** 🎉

혹시 Certbot 오류나 도메인 연결 문제 발생하면 알려주세요!