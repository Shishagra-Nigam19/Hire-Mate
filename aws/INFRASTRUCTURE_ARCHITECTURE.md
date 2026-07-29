# AWS Cloud Infrastructure & Architecture Specification

This document presents the AWS Cloud Architecture for **HireMate Platform**, engineered following the **AWS Well-Architected Framework** pillars: Security, Reliability, Performance Efficiency, Operational Excellence, and Cost Optimization.

---

## 1. Cloud Architecture Overview

```
                                    +-----------------------+
                                    |   INTERNET CLIENTS    |
                                    +-----------------------+
                                                |
                                                | HTTPS (Port 443)
                                                v
                                    +-----------------------+
                                    |    AWS ROUTE 53 DNS   |
                                    +-----------------------+
                                                |
                                                v
+---------------------------------------------------------------------------------------------------+
|                                      AWS VPC (10.0.0.0/16)                                        |
|                                                                                                   |
|  +---------------------------------------------------------------------------------------------+  |
|  |                       PUBLIC SUBNET 1a (10.0.1.0/24) - Internet Gateway                     |  |
|  |                                                                                             |  |
|  |  +---------------------------------------------------------------------------------------+  |  |
|  |  |                     AWS EC2 Instance (t3.medium / Amazon Linux 2023)                   |  |  |
|  |  |                                                                                       |  |  |
|  |  |   +--------------------------+    +-----------------------+    +------------------+   |  |  |
|  |  |   | Nginx Proxy (Port 80/443)|===>| Spring Boot App (:8080)|===>| Actuator Metrics |   |  |  |
|  |  |   +--------------------------+    +-----------------------+    +------------------+   |  |  |
|  |  +---------------------------------------------------------------------------------------+  |  |
|  +---------------------------------------------------------------------------------------------+  |
|                                                |                                                  |
|                                                | Private DB Connection (Port 5432)                |
|                                                v                                                  |
|  +---------------------------------------------------------------------------------------------+  |
|  |                       PRIVATE DB SUBNET 1b (10.0.2.0/24) - Isolated Subnet                  |  |
|  |                                                                                             |  |
|  |  +---------------------------------------------------------------------------------------+  |  |
|  |  |                         AWS RDS PostgreSQL (Multi-AZ Instance)                        |  |  |
|  |  |                         - PostgreSQL 16.2 / db.t4g.medium                             |  |  |
|  |  |                         - Storage Encrypted via AWS KMS (AES-256)                     |  |  |
|  |  +---------------------------------------------------------------------------------------+  |  |
|  +---------------------------------------------------------------------------------------------+  |
+---------------------------------------------------------------------------------------------------+
                                                 |
                                                 | HTTPS API / SDK (S3 Sign V4)
                                                 v
+---------------------------------------------------------------------------------------------------+
|                                       AWS S3 OBJECT STORAGE                                       |
|  - Bucket Name: hiremate-resumes-prod                                                             |
|  - Server-Side Encryption: SSE-S3 (AES-256)                                                       |
|  - Lifecycle Policy: Transition older resumes to S3 Glacier after 365 days                       |
+---------------------------------------------------------------------------------------------------+
```

---

## 2. AWS Components & Security Groups Setup

### A. AWS EC2 Security Group (`hiremate-ec2-sg`)
| Direction | Type | Port Range | Source | Purpose |
|---|---|---|---|---|
| Inbound | HTTP | 80 | `0.0.0.0/0` | Certbot & HTTP Redirection |
| Inbound | HTTPS | 443 | `0.0.0.0/0` | Secure SSL Traffic |
| Inbound | SSH | 22 | `ADMIN_IP/32` | Bastion Administration |
| Outbound | ALL | ALL | `0.0.0.0/0` | S3 & External API Access |

### B. AWS RDS Security Group (`hiremate-rds-sg`)
| Direction | Type | Port Range | Source | Purpose |
|---|---|---|---|---|
| Inbound | PostgreSQL | 5432 | `hiremate-ec2-sg` | Database Access exclusively from EC2 |
| Outbound | NONE | NONE | N/A | Fully Isolated Private Subnet |

---

## 3. AWS IAM Least-Privilege Policy for S3 Access

Attach the following policy to the EC2 IAM Role (`hiremate-ec2-role`):

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "HireMateS3BucketAccess",
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::hiremate-resumes-prod",
        "arn:aws:s3:::hiremate-resumes-prod/*"
      ]
    }
  ]
}
```

---

## 4. Operational Monitoring & Disaster Recovery

- **Automated RDS Snapshots**: Retention set to 7 days with point-in-time recovery (PITR) enabled.
- **Log Aggregation**: CloudWatch Agent streams `/var/log/nginx/access.log` and `/var/log/nginx/error.log` directly to CloudWatch Log Groups.
- **Metrics**: Spring Boot Prometheus endpoint (`/actuator/prometheus`) scraped for CPU utilization, JVM heap memory, thread pool exhaustion, and DB connection pool stats.
