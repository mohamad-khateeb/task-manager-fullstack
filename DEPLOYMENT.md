# Deployment Guide - Task Manager Application

## 📋 Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [System Requirements](#system-requirements)
3. [Infrastructure Components](#infrastructure-components)
4. [Deployment Architecture](#deployment-architecture)
5. [Step-by-Step Deployment](#step-by-step-deployment)
6. [Performance Optimization](#performance-optimization)
7. [Security Best Practices](#security-best-practices)
8. [Monitoring & Logging](#monitoring--logging)
9. [Scaling Strategy](#scaling-strategy)
10. [Disaster Recovery](#disaster-recovery)
11. [Cost Estimation](#cost-estimation)
12. [Maintenance & Updates](#maintenance--updates)

---

## 🏗️ Architecture Overview

This document outlines a production-ready deployment strategy for the Task Manager application designed to handle **10,000 users per day** with high availability, scalability, and security.

### Key Design Principles

- **High Availability**: Multi-AZ deployment with automatic failover
- **Scalability**: Auto-scaling based on demand
- **Security**: Defense in depth with multiple security layers
- **Performance**: CDN, caching, and optimized database queries
- **Cost-Effective**: Right-sized resources with cost optimization

### Traffic Estimation

- **Daily Users**: 10,000
- **Peak Concurrent Users**: ~500-1,000
- **Average Requests per User**: 50-100/day
- **Peak Requests per Second**: ~50-100 RPS
- **Database Operations**: ~500,000-1,000,000/day

---

## 📊 System Requirements

### Application Requirements

- **Backend**: Spring Boot 3.2.0 (Java 21)
- **Frontend**: React 19.2.0 (Static build)
- **Database**: PostgreSQL 15+
- **Authentication**: AWS Cognito User Pool
- **Container Runtime**: Docker

### Infrastructure Requirements

- **Compute**: 2-4 backend instances (2 vCPU, 4GB RAM each)
- **Database**: PostgreSQL (2 vCPU, 8GB RAM, 100GB storage)
- **CDN**: CloudFront distribution
- **Load Balancer**: Application Load Balancer
- **Storage**: S3 for static assets

---

## 🧩 Infrastructure Components

### 1. Frontend (React Application)

**Technology**: AWS S3 + CloudFront CDN

**Why This Approach**:
- **Global Distribution**: CloudFront edge locations reduce latency worldwide
- **Cost-Effective**: Pay only for data transfer and requests
- **HTTPS**: Free SSL certificates via AWS Certificate Manager
- **Caching**: Aggressive caching reduces origin requests
- **Scalability**: Automatically handles traffic spikes

**Configuration**:
- S3 bucket with static website hosting
- CloudFront distribution with custom domain
- Cache invalidation on deployments
- Compression enabled (Gzip/Brotli)

### 2. Backend (Spring Boot API)

**Technology**: AWS ECS Fargate (Container Service)

**Why ECS Fargate**:
- **Serverless Containers**: No EC2 management required
- **Auto-Scaling**: Automatic scaling based on metrics
- **High Availability**: Multi-AZ deployment
- **Cost-Effective**: Pay only for running containers
- **Easy Updates**: Rolling deployments with zero downtime

**Configuration**:
- Task Definition: 2 vCPU, 4GB RAM
- Service: 2-4 tasks (auto-scaling)
- Health Checks: `/api/auth/diagnostic` endpoint
- Logging: CloudWatch Logs integration

### 3. Database (PostgreSQL)

**Technology**: AWS RDS PostgreSQL

**Why RDS**:
- **Managed Service**: Automated backups, patching, monitoring
- **High Availability**: Multi-AZ deployment with automatic failover
- **Performance**: Optimized for PostgreSQL workloads
- **Security**: Encryption at rest and in transit
- **Backup**: Automated daily backups with point-in-time recovery

**Configuration**:
- Instance Class: `db.t3.medium` (2 vCPU, 4GB RAM) or `db.t3.large` (2 vCPU, 8GB RAM)
- Storage: 100GB GP3 SSD (scalable)
- Multi-AZ: Enabled for high availability
- Backup Retention: 7 days
- Automated Backups: Daily at 03:00 UTC

### 4. Authentication

**Technology**: AWS Cognito User Pool (Already Configured)

**Features**:
- User authentication and authorization
- JWT token generation and validation
- User groups (ADMIN, USER) for RBAC
- Password policies and MFA support

### 5. Load Balancing

**Technology**: Application Load Balancer (ALB)

**Why ALB**:
- **Path-Based Routing**: Route requests to appropriate services
- **SSL Termination**: Handle HTTPS certificates
- **Health Checks**: Automatic unhealthy instance removal
- **Sticky Sessions**: Optional session affinity
- **Integration**: Seamless ECS integration

### 6. Caching (Optional but Recommended)

**Technology**: Amazon ElastiCache (Redis)

**Use Cases**:
- Session caching
- Token caching
- Frequently accessed data
- Rate limiting

**Configuration**:
- Instance Type: `cache.t3.micro` or `cache.t3.small`
- Multi-AZ: Enabled
- Automatic Failover: Enabled

---

## 🏛️ Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Internet Users                          │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ HTTPS (Port 443)
                             │
                    ┌────────▼─────────┐
                    │   CloudFront     │  ← CDN for Frontend
                    │   Distribution   │     (Global Edge Locations)
                    └────────┬─────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
        ┌───────▼────────┐        ┌───────▼────────┐
        │   S3 Bucket    │        │  ALB (HTTPS)   │  ← Load Balancer
        │   (Frontend)   │        │  (Backend API) │
        │   React Build  │        └───────┬────────┘
        └────────────────┘                │
                                           │
                    ┌──────────────────────┼──────────────────────┐
                    │                      │                      │
            ┌───────▼────────┐    ┌───────▼────────┐    ┌───────▼────────┐
            │  ECS Fargate   │    │  ECS Fargate   │    │  ECS Fargate   │
            │  Task 1        │    │  Task 2        │    │  Task 3        │
            │  (Backend)     │    │  (Backend)     │    │  (Backend)     │
            │  Spring Boot   │    │  Spring Boot   │    │  Spring Boot   │
            └───────┬────────┘    └───────┬────────┘    └───────┬────────┘
                    │                      │                      │
                    └──────────────────────┼──────────────────────┘
                                           │
                    ┌──────────────────────┼──────────────────────┐
                    │                      │                      │
            ┌───────▼────────┐    ┌───────▼────────┐    ┌───────▼────────┐
            │  RDS Primary   │    │  RDS Standby   │    │  ElastiCache   │
            │  (Multi-AZ)    │◄───│  (Multi-AZ)    │    │  (Redis)       │
            │  PostgreSQL    │    │  PostgreSQL    │    │  (Optional)    │
            └────────────────┘    └────────────────┘    └────────────────┘
                    │
            ┌───────▼────────┐
            │  AWS Cognito   │
            │  User Pool     │
            └────────────────┘
```

### Network Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      VPC (10.0.0.0/16)                      │
│                                                             │
│  ┌──────────────────┐         ┌──────────────────┐      │
│  │  Public Subnet   │         │  Public Subnet   │      │
│  │  (10.0.1.0/24)   │         │  (10.0.2.0/24)   │      │
│  │                  │         │                  │      │
│  │  - ALB           │         │  - NAT Gateway   │      │
│  │  - CloudFront    │         │                  │      │
│  └──────────────────┘         └──────────────────┘      │
│                                                             │
│  ┌──────────────────┐         ┌──────────────────┐      │
│  │  Private Subnet  │         │  Private Subnet  │      │
│  │  (10.0.3.0/24)   │         │  (10.0.4.0/24)   │      │
│  │                  │         │                  │      │
│  │  - ECS Tasks     │         │  - ECS Tasks     │      │
│  │  - RDS Primary   │         │  - RDS Standby   │      │
│  │  - ElastiCache   │         │  - ElastiCache   │      │
│  └──────────────────┘         └──────────────────┘      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Step-by-Step Deployment

### Prerequisites

1. **AWS Account** with appropriate IAM permissions
2. **AWS CLI** installed and configured
3. **Docker** installed locally
4. **Domain Name** (optional, for custom domain)
5. **SSL Certificate** (AWS Certificate Manager)

### Phase 1: Database Setup

#### Step 1.1: Create RDS PostgreSQL Instance

```bash
# Using AWS CLI
aws rds create-db-instance \
  --db-instance-identifier task-manager-db \
  --db-instance-class db.t3.medium \
  --engine postgres \
  --engine-version 15.4 \
  --master-username postgres \
  --master-user-password <SECURE_PASSWORD> \
  --allocated-storage 100 \
  --storage-type gp3 \
  --vpc-security-group-ids sg-xxxxxxxxx \
  --db-subnet-group-name task-manager-subnet-group \
  --backup-retention-period 7 \
  --multi-az \
  --storage-encrypted \
  --publicly-accessible false
```

**Or using AWS Console**:
1. Navigate to RDS → Databases → Create Database
2. Select PostgreSQL
3. Choose "Production" template
4. Configure:
   - DB Instance: `db.t3.medium`
   - Storage: 100GB GP3
   - Multi-AZ: Yes
   - Master username: `postgres`
   - Master password: (strong password)
   - VPC: Your VPC
   - Security Group: Allow port 5432 from ECS tasks only
5. Enable encryption at rest
6. Set backup retention to 7 days

#### Step 1.2: Configure Database

```sql
-- Connect to RDS instance
psql -h <RDS_ENDPOINT> -U postgres -d postgres

-- Create application database
CREATE DATABASE taskdb;

-- Create application user (optional, for better security)
CREATE USER taskapp WITH PASSWORD '<SECURE_PASSWORD>';
GRANT ALL PRIVILEGES ON DATABASE taskdb TO taskapp;
```

#### Step 1.3: Update Application Configuration

Update `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://<RDS_ENDPOINT>:5432/taskdb
    username: postgres
    password: ${DB_PASSWORD}  # Use environment variable or AWS Secrets Manager
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false  # Disable in production
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false
```

### Phase 2: Container Setup

#### Step 2.1: Create Dockerfile

Create `backend/Dockerfile`:

```dockerfile
# Multi-stage build for optimized image size
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Step 2.2: Create .dockerignore

Create `backend/.dockerignore`:

```
target/
.mvn/
mvnw
mvnw.cmd
*.iml
.idea/
.git/
.gitignore
```

#### Step 2.3: Build and Test Docker Image

```bash
cd backend
docker build -t task-manager-backend:latest .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<RDS_ENDPOINT>:5432/taskdb \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=<PASSWORD> \
  task-manager-backend:latest
```

### Phase 3: ECS Setup

#### Step 3.1: Create ECR Repository

```bash
aws ecr create-repository --repository-name task-manager-backend --region us-east-1
```

#### Step 3.2: Push Docker Image to ECR

```bash
# Get login token
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com

# Tag image
docker tag task-manager-backend:latest <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/task-manager-backend:latest

# Push image
docker push <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/task-manager-backend:latest
```

#### Step 3.3: Create ECS Task Definition

Create `ecs-task-definition.json`:

```json
{
  "family": "task-manager-backend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "2048",
  "memory": "4096",
  "executionRoleArn": "arn:aws:iam::<ACCOUNT_ID>:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::<ACCOUNT_ID>:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "task-manager-backend",
      "image": "<ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/task-manager-backend:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "production"
        }
      ],
      "secrets": [
        {
          "name": "SPRING_DATASOURCE_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:<ACCOUNT_ID>:secret:task-manager/db-password"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/task-manager-backend",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "curl -f http://localhost:8080/api/auth/diagnostic || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

Register task definition:

```bash
aws ecs register-task-definition --cli-input-json file://ecs-task-definition.json
```

#### Step 3.4: Create ECS Cluster

```bash
aws ecs create-cluster --cluster-name task-manager-cluster
```

#### Step 3.5: Create ECS Service

```bash
aws ecs create-service \
  --cluster task-manager-cluster \
  --service-name task-manager-backend \
  --task-definition task-manager-backend \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx,subnet-yyy],securityGroups=[sg-xxx],assignPublicIp=DISABLED}" \
  --load-balancers "targetGroupArn=arn:aws:elasticloadbalancing:us-east-1:<ACCOUNT_ID>:targetgroup/task-manager-tg/xxx,containerName=task-manager-backend,containerPort=8080" \
  --enable-execute-command
```

### Phase 4: Load Balancer Setup

#### Step 4.1: Create Application Load Balancer

```bash
aws elbv2 create-load-balancer \
  --name task-manager-alb \
  --subnets subnet-xxx subnet-yyy \
  --security-groups sg-xxx \
  --scheme internet-facing \
  --type application
```

#### Step 4.2: Create Target Group

```bash
aws elbv2 create-target-group \
  --name task-manager-tg \
  --protocol HTTP \
  --port 8080 \
  --vpc-id vpc-xxx \
  --target-type ip \
  --health-check-path /api/auth/diagnostic \
  --health-check-interval-seconds 30 \
  --health-check-timeout-seconds 5 \
  --healthy-threshold-count 2 \
  --unhealthy-threshold-count 3
```

#### Step 4.3: Create Listener

```bash
aws elbv2 create-listener \
  --load-balancer-arn <ALB_ARN> \
  --protocol HTTPS \
  --port 443 \
  --certificates CertificateArn=<CERT_ARN> \
  --default-actions Type=forward,TargetGroupArn=<TARGET_GROUP_ARN>
```

### Phase 5: Frontend Deployment

#### Step 5.1: Build React Application

```bash
cd frontend
npm install
npm run build
```

#### Step 5.2: Create S3 Bucket

```bash
aws s3 mb s3://task-manager-frontend --region us-east-1
aws s3 website s3://task-manager-frontend --index-document index.html --error-document index.html
```

#### Step 5.3: Upload Frontend Build

```bash
aws s3 sync build/ s3://task-manager-frontend --delete
```

#### Step 5.4: Create CloudFront Distribution

```bash
aws cloudfront create-distribution \
  --distribution-config file://cloudfront-config.json
```

CloudFront configuration (`cloudfront-config.json`):

```json
{
  "CallerReference": "task-manager-frontend-2024",
  "Comment": "Task Manager Frontend Distribution",
  "DefaultCacheBehavior": {
    "TargetOriginId": "S3-task-manager-frontend",
    "ViewerProtocolPolicy": "redirect-to-https",
    "AllowedMethods": {
      "Quantity": 2,
      "Items": ["GET", "HEAD"]
    },
    "Compress": true,
    "ForwardedValues": {
      "QueryString": false,
      "Cookies": {
        "Forward": "none"
      }
    },
    "MinTTL": 0,
    "DefaultTTL": 86400,
    "MaxTTL": 31536000
  },
  "Origins": {
    "Quantity": 1,
    "Items": [
      {
        "Id": "S3-task-manager-frontend",
        "DomainName": "task-manager-frontend.s3.amazonaws.com",
        "S3OriginConfig": {
          "OriginAccessIdentity": ""
        }
      }
    ]
  },
  "Enabled": true,
  "PriceClass": "PriceClass_100"
}
```

#### Step 5.5: Update Frontend API Endpoint

Update `frontend/src/services/api.js` to use ALB endpoint:

```javascript
const API_BASE_URL = process.env.REACT_APP_API_URL || 'https://api.yourdomain.com';
```

Rebuild and redeploy:

```bash
npm run build
aws s3 sync build/ s3://task-manager-frontend --delete
aws cloudfront create-invalidation --distribution-id <DIST_ID> --paths "/*"
```

### Phase 6: Auto-Scaling Configuration

#### Step 6.1: Create Auto-Scaling Target

```bash
aws application-autoscaling register-scalable-target \
  --service-namespace ecs \
  --resource-id service/task-manager-cluster/task-manager-backend \
  --scalable-dimension ecs:service:DesiredCount \
  --min-capacity 2 \
  --max-capacity 10
```

#### Step 6.2: Create Scaling Policy

```bash
aws application-autoscaling put-scaling-policy \
  --service-namespace ecs \
  --resource-id service/task-manager-cluster/task-manager-backend \
  --scalable-dimension ecs:service:DesiredCount \
  --policy-name cpu-scaling-policy \
  --policy-type TargetTrackingScaling \
  --target-tracking-scaling-policy-configuration '{
    "TargetValue": 70.0,
    "PredefinedMetricSpecification": {
      "PredefinedMetricType": "ECSServiceAverageCPUUtilization"
    },
    "ScaleInCooldown": 300,
    "ScaleOutCooldown": 60
  }'
```

---

## ⚡ Performance Optimization

### 1. Database Optimization

#### Connection Pooling
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

#### Query Optimization
- Add database indexes on frequently queried columns
- Use `EXPLAIN ANALYZE` to optimize slow queries
- Enable query result caching for read-heavy operations

#### Read Replicas (Optional)
For read-heavy workloads, create RDS read replicas:

```bash
aws rds create-db-instance-read-replica \
  --db-instance-identifier task-manager-db-replica \
  --source-db-instance-identifier task-manager-db
```

### 2. Application-Level Caching

#### Redis Caching
Add Spring Cache with Redis:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```yaml
spring:
  cache:
    type: redis
  redis:
    host: <ELASTICACHE_ENDPOINT>
    port: 6379
    password: ${REDIS_PASSWORD}
```

### 3. CDN Optimization

- **Cache-Control Headers**: Set appropriate cache headers
- **Compression**: Enable Gzip/Brotli compression
- **Edge Locations**: Use CloudFront edge locations globally

### 4. API Response Optimization

- **Pagination**: Already implemented (default page size: 20)
- **Response Compression**: Enable in Spring Boot
- **ETags**: Implement for conditional requests

---

## 🔒 Security Best Practices

### 1. Network Security

#### VPC Configuration
- **Private Subnets**: ECS tasks and RDS in private subnets
- **Security Groups**: Restrictive rules (least privilege)
- **NAT Gateway**: For outbound internet access from private subnets

#### Security Group Rules

**ALB Security Group**:
- Inbound: HTTPS (443) from 0.0.0.0/0
- Outbound: All traffic

**ECS Security Group**:
- Inbound: HTTP (8080) from ALB security group only
- Outbound: HTTPS (443) to RDS and Cognito

**RDS Security Group**:
- Inbound: PostgreSQL (5432) from ECS security group only
- Outbound: None

### 2. Application Security

#### Secrets Management
Use AWS Secrets Manager for sensitive data:

```bash
aws secretsmanager create-secret \
  --name task-manager/db-password \
  --secret-string "<DB_PASSWORD>"
```

#### Environment Variables
Never commit secrets to code. Use environment variables or Secrets Manager.

#### HTTPS Everywhere
- CloudFront: HTTPS only
- ALB: HTTPS with SSL certificate
- RDS: SSL/TLS connections

### 3. Database Security

- **Encryption at Rest**: Enabled on RDS
- **Encryption in Transit**: SSL/TLS connections
- **Backup Encryption**: Automated backups encrypted
- **Network Isolation**: RDS in private subnet
- **Access Control**: Least privilege database users

### 4. Container Security

- **Image Scanning**: Scan Docker images for vulnerabilities
- **Non-Root User**: Run containers as non-root user
- **Minimal Base Image**: Use Alpine Linux base images
- **Regular Updates**: Keep base images updated

### 5. Monitoring & Alerting

- **CloudWatch Alarms**: Monitor for suspicious activity
- **VPC Flow Logs**: Enable for network monitoring
- **AWS GuardDuty**: Enable for threat detection (optional)

---

## 📊 Monitoring & Logging

### 1. CloudWatch Logs

#### Log Groups
- `/ecs/task-manager-backend`: Application logs
- `/aws/rds/postgresql`: Database logs (if enabled)

#### Log Retention
- Application logs: 30 days
- Database logs: 7 days
- Access logs: 90 days

### 2. CloudWatch Metrics

#### Key Metrics to Monitor

**Application Metrics**:
- Request count
- Response time (p50, p95, p99)
- Error rate (4xx, 5xx)
- Active connections

**Infrastructure Metrics**:
- CPU utilization (ECS tasks)
- Memory utilization (ECS tasks)
- Database connections (RDS)
- Database CPU/Memory (RDS)
- ALB request count
- ALB response time

#### CloudWatch Dashboards

Create custom dashboard with:
- Request rate and latency
- Error rates
- Database performance
- Container health
- Cost metrics

### 3. Application Performance Monitoring (APM)

Consider using:
- **AWS X-Ray**: Distributed tracing
- **New Relic**: Full-stack APM (optional)
- **Datadog**: Infrastructure monitoring (optional)

### 4. Alarms

Set up CloudWatch alarms for:
- High error rate (> 5%)
- High response time (> 2 seconds)
- High CPU utilization (> 80%)
- Database connection pool exhaustion
- Low disk space (< 20%)

---

## 📈 Scaling Strategy

### Horizontal Scaling (ECS)

**Auto-Scaling Triggers**:
- CPU utilization > 70%: Scale out
- CPU utilization < 30%: Scale in
- Memory utilization > 80%: Scale out
- Request count: Scale based on ALB request count

**Scaling Limits**:
- Minimum: 2 tasks (for high availability)
- Maximum: 10 tasks (for 10k users/day)
- Desired: 2-4 tasks (normal load)

### Database Scaling

**Vertical Scaling**:
- Start with `db.t3.medium`
- Scale to `db.t3.large` if needed
- Scale to `db.r5.large` for better performance

**Horizontal Scaling**:
- Add read replicas for read-heavy workloads
- Use connection pooling to manage connections

### CDN Scaling

CloudFront automatically scales to handle traffic spikes.

---

## 🛡️ Disaster Recovery

### Backup Strategy

#### Database Backups
- **Automated Backups**: Daily at 03:00 UTC
- **Retention**: 7 days
- **Point-in-Time Recovery**: Enabled
- **Manual Snapshots**: Before major deployments

#### Application Backups
- **Docker Images**: Stored in ECR (versioned)
- **Configuration**: Stored in version control (Git)
- **Infrastructure**: Defined as code (CloudFormation/Terraform)

### Recovery Procedures

#### Database Recovery
1. Identify the point in time to restore
2. Create new RDS instance from backup
3. Update application configuration
4. Verify data integrity
5. Switch traffic to new instance

#### Application Recovery
1. Deploy previous Docker image version
2. Rollback ECS service to previous task definition
3. Verify application health
4. Monitor for issues

### RTO/RPO Targets

- **RTO (Recovery Time Objective)**: < 1 hour
- **RPO (Recovery Point Objective)**: < 15 minutes (with PITR)

---

## 💰 Cost Estimation

### Monthly Cost Breakdown (US East - N. Virginia)

#### Infrastructure Costs

| Service | Configuration | Monthly Cost |
|---------|--------------|--------------|
| **ECS Fargate** | 2 tasks × 2 vCPU × 4GB (24/7) | ~$60 |
| **RDS PostgreSQL** | db.t3.medium, Multi-AZ, 100GB | ~$150 |
| **Application Load Balancer** | Standard ALB | ~$25 |
| **S3 Storage** | 1GB storage, 10k requests | ~$1 |
| **CloudFront** | 50GB transfer, 1M requests | ~$10 |
| **ElastiCache Redis** | cache.t3.micro (optional) | ~$15 |
| **Data Transfer** | Inter-AZ and outbound | ~$20 |
| **CloudWatch Logs** | 10GB log ingestion | ~$5 |
| **Secrets Manager** | 1 secret | ~$0.40 |
| **Total** | | **~$286/month** |

#### Cost Optimization Tips

1. **Reserved Instances**: Save up to 40% on RDS with 1-year commitment
2. **Spot Instances**: Use for non-critical workloads (not applicable to Fargate)
3. **Right-Sizing**: Monitor and adjust instance sizes
4. **S3 Lifecycle Policies**: Move old logs to Glacier
5. **CloudFront Caching**: Reduce origin requests
6. **Auto-Scaling**: Scale down during low-traffic periods

### Cost Scaling for Growth

- **10k users/day**: ~$286/month
- **50k users/day**: ~$500-700/month
- **100k users/day**: ~$1,000-1,500/month

---

## 🔧 Maintenance & Updates

### Deployment Process

1. **Development**: Test changes locally
2. **Staging**: Deploy to staging environment
3. **Testing**: Run integration tests
4. **Production**: Blue-green or rolling deployment

### Update Procedures

#### Backend Updates
```bash
# Build new image
docker build -t task-manager-backend:v1.1.0 .

# Push to ECR
docker push <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/task-manager-backend:v1.1.0

# Update ECS service
aws ecs update-service \
  --cluster task-manager-cluster \
  --service task-manager-backend \
  --force-new-deployment
```

#### Frontend Updates
```bash
# Build
npm run build

# Deploy to S3
aws s3 sync build/ s3://task-manager-frontend --delete

# Invalidate CloudFront cache
aws cloudfront create-invalidation \
  --distribution-id <DIST_ID> \
  --paths "/*"
```

#### Database Migrations
- Use Flyway or Liquibase for schema migrations
- Test migrations in staging first
- Backup database before migrations
- Run migrations during low-traffic periods

### Health Checks

- **Application**: `/api/auth/diagnostic`
- **Database**: Connection pool health
- **Load Balancer**: Target group health checks

### Rollback Plan

1. Keep previous Docker image version
2. Keep previous task definition
3. Keep database backups
4. Document rollback procedures
5. Test rollback in staging

---

## 📝 Additional Considerations

### Custom Domain Setup

1. **Route 53**: Create hosted zone
2. **Certificate**: Request SSL certificate in ACM
3. **CloudFront**: Add custom domain
4. **ALB**: Add custom domain (optional)
5. **DNS**: Update Route 53 records

### CI/CD Pipeline

Consider setting up:
- **GitHub Actions** or **AWS CodePipeline**
- Automated testing on pull requests
- Automated deployment to staging
- Manual approval for production

### Environment Variables

Use different configurations for:
- Development
- Staging
- Production

### Documentation

Maintain documentation for:
- Architecture diagrams
- Deployment procedures
- Troubleshooting guides
- Runbooks for common issues

---

## ✅ Deployment Checklist

- [ ] RDS PostgreSQL instance created and configured
- [ ] Database migrations applied
- [ ] Docker image built and tested
- [ ] ECR repository created and image pushed
- [ ] ECS cluster and service created
- [ ] Application Load Balancer configured
- [ ] Security groups configured correctly
- [ ] SSL certificates installed
- [ ] Frontend built and deployed to S3
- [ ] CloudFront distribution created
- [ ] Auto-scaling configured
- [ ] CloudWatch alarms set up
- [ ] Secrets stored in Secrets Manager
- [ ] Health checks configured
- [ ] Monitoring dashboards created
- [ ] Backup strategy implemented
- [ ] Disaster recovery plan documented
- [ ] Cost monitoring enabled
- [ ] Documentation updated

---

## 🎯 Conclusion

This deployment architecture provides:

✅ **High Availability**: Multi-AZ deployment with automatic failover  
✅ **Scalability**: Auto-scaling to handle traffic spikes  
✅ **Security**: Multiple security layers and best practices  
✅ **Performance**: Optimized for 10k users/day with room to grow  
✅ **Cost-Effective**: Right-sized resources with optimization  
✅ **Maintainability**: Clear procedures and documentation  

The system is designed to handle **10,000 users per day** comfortably and can scale to **50,000+ users** with minimal configuration changes.

---

## 📚 References

- [AWS ECS Best Practices](https://docs.aws.amazon.com/AmazonECS/latest/bestpracticesguide/)
- [AWS RDS Best Practices](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_BestPractices.html)
- [Spring Boot Production Ready](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [PostgreSQL Performance Tuning](https://wiki.postgresql.org/wiki/Performance_Optimization)

---

**Last Updated**: November 2024  
**Version**: 1.0  
**Author**: Task Manager Development Team

