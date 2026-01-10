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

## 🔐 Step 3: HTTPS (SSL) 설정 with Certbot

```bash
# EC2에서
cd ~/recipemate

# 1. Certbot Docker 이미지로 인증서 발급
docker run -it --rm --network host \
  -v /home/ec2-user/recipemate/nginx/ssl:/etc/letsencrypt \
  certbot/certbot certonly --standalone \
  -d recipemate.duckdns.org \
  --email person3113@gmail.com \
  --agree-tos --non-interactive

# 2. 인증서 확인
sudo ls -la ~/recipemate/nginx/ssl/live/recipemate.duckdns.org/
# fullchain.pem, privkey.pem 파일 생성됨

# 3. Docker Compose 재시작
docker compose down
docker compose up -d --build --force-recreate

# 4. 인증서 자동 갱신 (3개월마다)
(crontab -l 2>/dev/null; echo "0 3 1 * * cd ~/recipemate && docker compose stop nginx && docker run --rm -v ./nginx/ssl:/etc/letsencrypt certbot/certbot renew --quiet && docker compose up -d nginx") | crontab -
```

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
