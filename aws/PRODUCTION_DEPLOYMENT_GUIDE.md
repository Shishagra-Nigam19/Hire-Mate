# AWS Production Cloud Deployment Guide - HireMate Platform

This guide provides a step-by-step production deployment runbook for deploying **HireMate** onto **AWS Infrastructure** (EC2, RDS PostgreSQL, S3, Nginx, Let's Encrypt, and GitHub Actions CI/CD).

---

## Step 1: AWS Infrastructure Provisioning

### 1. Create AWS S3 Bucket
1. Go to AWS S3 Console -> Create Bucket named `hiremate-resumes-prod`.
2. Enable **Block *all* public access**.
3. Enable **Server-side encryption** with Amazon S3 managed keys (SSE-S3).

### 2. Provision AWS RDS PostgreSQL Database
1. Go to AWS RDS Console -> Create Database -> PostgreSQL 16.
2. Template: **Production** (Multi-AZ deployment).
3. DB Instance Class: `db.t4g.medium`.
4. Master Username: `postgres_user`.
5. VPC: Select your Production VPC and place in **Private DB Subnets**.
6. Security Group: Create `hiremate-rds-sg` allowing Port 5432 inbound from `hiremate-ec2-sg`.

### 3. Launch AWS EC2 Instance
1. Go to AWS EC2 Console -> Launch Instance.
2. AMI: **Amazon Linux 2023** or **Ubuntu 24.04 LTS**.
3. Instance Type: `t3.medium` (2 vCPU, 4GB RAM).
4. Key Pair: Select or create SSH key pair (`hiremate-key.pem`).
5. Network Settings: Assign Public IP; attach `hiremate-ec2-sg` allowing Ports 80, 443, and SSH (Port 22).
6. IAM Role: Attach `hiremate-ec2-role` with S3 access permissions.

---

## Step 2: EC2 Server Initialization

SSH into your EC2 instance:
```bash
ssh -i hiremate-key.pem ubuntu@YOUR_EC2_PUBLIC_IP
```

Install Docker, Docker Compose, and Git:
```bash
# Update System Packages
sudo apt-get update && sudo apt-get upgrade -y

# Install Docker Engine & Compose
sudo apt-get install -y docker.io docker-compose git curl

# Enable Docker Service
sudo systemctl enable --now docker
sudo usermod -aG docker $USER

# Log out and log back in to apply group changes
exit
```

Clone Repository onto EC2:
```bash
sudo mkdir -p /opt/hiremate
sudo chown -R $USER:$USER /opt/hiremate
cd /opt/hiremate
git clone https://github.com/Shishagra-Nigam19/Hire-Mate.git .
```

---

## Step 3: Configure Environment Variables & Secrets

Copy the `.env.example` file to `.env`:
```bash
cp .env.example .env
nano .env
```

Configure your production secrets:
```env
SPRING_PROFILES_ACTIVE=prod
PORT=8080
POSTGRES_DB=hiremate_db
POSTGRES_USER=postgres_user
POSTGRES_PASSWORD=YOUR_AWS_RDS_PASSWORD
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_RDS_ENDPOINT.rds.amazonaws.com:5432/hiremate_db
JWT_SECRET=YOUR_SECURE_256BIT_SECRET_KEY
AWS_REGION=us-east-1
AWS_S3_BUCKET=hiremate-resumes-prod
AWS_ACCESS_KEY_ID=YOUR_AWS_ACCESS_KEY
AWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET_KEY
ALLOWED_ORIGINS=https://hiremate-api.com
```

---

## Step 4: Issue TLS Certificates & Launch Application

Make scripts executable:
```bash
chmod +x scripts/*.sh
```

Generate Let's Encrypt TLS Certificate:
```bash
./scripts/init-letsencrypt.sh
```

Launch Production Multi-Container Stack:
```bash
./scripts/deploy.sh
```

---

## Step 5: Configure GitHub Actions CI/CD Pipeline

In your GitHub Repository -> **Settings** -> **Secrets and variables** -> **Actions**, add:

| Secret Name | Value Description |
|---|---|
| `DOCKER_USERNAME` | Docker Hub / ECR Username |
| `DOCKER_PASSWORD` | Docker Hub Personal Access Token / ECR Password |
| `EC2_HOST` | Public Elastic IP address of EC2 instance |
| `EC2_USER` | `ubuntu` or `ec2-user` |
| `EC2_SSH_KEY` | Private Key content (`hiremate-key.pem`) |

Whenever code is pushed to `main`, GitHub Actions will automatically run unit tests, enforce 80% JaCoCo coverage, build Docker containers, and trigger zero-downtime EC2 deployment!

---

## Step 6: Operations & Troubleshooting Runbook

### Check Container Status
```bash
docker-compose -f docker-compose.prod.yml ps
```

### View Real-Time Logs
```bash
docker-compose -f docker-compose.prod.yml logs -f backend
```

### Manual Trigger Database Backup
```bash
./scripts/backup-db.sh
```

### Emergency Rollback
```bash
./scripts/rollback.sh
```
