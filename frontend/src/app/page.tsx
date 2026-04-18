import React from 'react';
import styles from './page.module.css';
import { Briefcase, CheckCircle, FileText, Target, GraduationCap, BarChart, Search, ChevronRight } from 'lucide-react';
import Link from 'next/link';
import { UserButton } from "@clerk/nextjs";

export default function Home() {
  const features = [
    { id: 'resume-optimization', title: 'Resume Optimization', desc: 'Enhance your resume to bypass ATS filters and catch recruiter attention.', icon: <FileText size={32}/> },
    { id: 'interview-prep', title: 'Interview Prep', desc: 'Practice mock interviews and get tips for behavioral and technical questions.', icon: <Target size={32}/> },
    { id: 'career-guidance', title: 'Career Guidance', desc: 'Get personalized advice on career paths, transitions, and growth.', icon: <GraduationCap size={32}/> },
    { id: 'job-matcher', title: 'Job Matcher', desc: 'Find the best job titles and roles that fit your completely unique skill set.', icon: <Briefcase size={32}/> },
    { id: 'skill-gap', title: 'Skill Gap Analysis', desc: 'Discover what skills you are missing for your dream job and how to learn them.', icon: <CheckCircle size={32}/> },
    { id: 'ats-score', title: 'ATS Score Checker', desc: 'Simulate an Applicant Tracking System to see how well your resume matches a job.', icon: <BarChart size={32}/> },
    { id: 'web-job-search', title: 'Live Web Job Search', desc: 'Our AI scours the internet for live job postings matching your resume and gives you direct apply links.', icon: <Search size={32}/>, href: '/jobs' },
  ];

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div className={styles.logo}>
          <Briefcase className="gradient-text" size={32} />
          <span className="gradient-text">HireMate</span>
        </div>
        <div style={{display: 'flex', alignItems: 'center', gap: '1.5rem'}}>
          <UserButton />
        </div>
      </header>

      <main style={{display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', padding: '2rem 0', gap: '2rem'}}>
        <div>
          <h1 style={{fontSize: '3rem', marginBottom: '1rem'}}>Launch Your Career with AI</h1>
          <p style={{color: 'var(--text-muted)', fontSize: '1.2rem', maxWidth: '600px', margin: '0 auto'}}>
            Select a specialized AI agent below to assist you with every step of the job placement process.
          </p>
        </div>

        <div style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem', width: '100%', maxWidth: '1000px', marginTop:'2rem'}}>
          {features.map((f) => (
            <Link href={f.href || `/${f.id}`} key={f.id} style={{textDecoration: 'none'}}>
              <div className="glass-panel" style={{display: 'flex', flexDirection: 'column', alignItems: 'flex-start', padding: '2rem', borderRadius: '16px', transition: 'all 0.3s ease', cursor: 'pointer', textAlign: 'left', height: '100%'}}>
                <div style={{color: 'var(--secondary)', marginBottom: '1.5rem', padding: '1rem', background: 'rgba(255,255,255,0.05)', borderRadius: '12px'}}>
                  {f.icon}
                </div>
                <h3 style={{fontSize: '1.25rem', marginBottom: '0.75rem', color: 'var(--text-main)'}}>{f.title}</h3>
                <p style={{color: 'var(--text-muted)', fontSize: '0.9rem', lineHeight: '1.6', marginBottom: '1.5rem', flex: 1}}>{f.desc}</p>
                <div style={{display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--primary)', fontSize: '0.9rem', fontWeight: 'bold', marginTop: 'auto'}}>
                  Explore Feature <ChevronRight size={16}/>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </main>
    </div>
  );
}
