<div align="center">
  <img src="assets/banner.png" alt="HireMate Banner" width="100%" />
  <br/><br/>
  <img src="https://lucide.dev/api/icons/briefcase?size=64&color=8b5cf6" alt="HireMate Logo" width="80" />
  <h1>HireMate</h1>
  <p><b>The Ultimate AI-Powered Career Copilot</b></p>

  <p>
    <img src="https://img.shields.io/badge/Next.js-15-black?style=for-the-badge&logo=next.js" alt="Next.js" />
    <img src="https://img.shields.io/badge/FastAPI-005571?style=for-the-badge&logo=fastapi" alt="FastAPI" />
    <img src="https://img.shields.io/badge/Google_Gemini-8E75B2?style=for-the-badge&logo=google-gemini" alt="Gemini" />
    <img src="https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=vercel" alt="Vercel" />
  </p>
</div>

---

## 🌟 Overview

**HireMate** is a comprehensive AI-driven platform designed to simplify the job hunt process and empower candidates with professional-grade career tools. Built with a focus on modern aesthetics and high-performance AI, HireMate serves as your personal 24/7 career counselor.

### 🔗 Live Access
- **Frontend Dashboard**: [https://hire-mate-26fv-shishagra-nigam19s-projects.vercel.app](https://hire-mate-26fv-3jjwxcofj-shishagra-nigam19s-projects.vercel.app/)
- **Backend API**: [https://hire-mate-backend.onrender.com](https://hire-mate-backend.onrender.com/)

---

## 🚀 Core Agents & Features

### 📄 1. The Resume Optimizer
*   **Goal**: Get your resume past the "Black Hole" of ATS.
*   **Action**: Analyzes your resume PDF against a specific Job Description.
<img src="assets/dashboard.png" width="600" alt="Resume Optimizer" />

### 🎯 2. Interview Prep Coach
*   **Goal**: Master your behavioral and technical presence.
*   **Action**: Real-time mock interviews with instant AI feedback.
<img src="assets/interview.png" width="600" alt="Interview Coach" />

### 🔍 3. Live Web Job search
*   **Goal**: Find hidden gems in real-time.
*   **Action**: Scours the internet for matched job postings.

### 🎓 4. Career Guidance Counselor
*   **Goal**: Personalized advice on transitions.
<img src="assets/chat.png" width="600" alt="Career Guidance" />

### 📈 5. ATS Score Simulator
*   **Goal**: Predict your ranking before you apply.
<img src="assets/analysis.png" width="600" alt="ATS Analysis" />

---

## 📸 Gallery

<div align="center">
  <h3>✨ The command center (Dashboard)</h3>
  <img src="assets/dashboard.png" width="800" alt="Dashboard" />
  <br/><br/>
  <h3>🎯 AI-Powered Interview Coach</h3>
  <img src="assets/interview.png" width="800" alt="Interview Prep" />
  <br/><br/>
  <h3>💬 Personalized Career Roadmap</h3>
  <img src="assets/chat.png" width="800" alt="Career Guidance" />
  <br/><br/>
  <h3>🧪 Depth Analysis (ATS Match)</h3>
  <img src="assets/analysis.png" width="800" alt="ATS Analysis" />
</div>

---

## 🛠️ Technical Architecture

- **Frontend**: Next.js 15 (App Router) + React 19 + Clerk Auth + Lucide Icons.
- **Backend**: FastAPI (Python) + Google Gemini Pro (LLM).
- **Styling**: Hand-crafted Glassmorphism CSS with CSS Variables.
- **Infrastructure**: Automated CI/CD via Vercel (Frontend) and Render (Backend).

---

## 💻 Local Setup

Want to contribute or run it locally?

### 1. Backend Setup
```bash
cd backend
python -m venv venv
./venv/Scripts/activate
pip install -r requirements.txt
python -m uvicorn main:app --reload
```

### 2. Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

### 3. Environment Config
Create a `.env` in the backend with:
`GEMINI_API_KEY=your_key_here`

Create a `.env.local` in the frontend with:
`NEXT_PUBLIC_API_URL=http://localhost:8000`
`NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=your_clerk_key`

---

## 🤝 Contributing
Contributions are welcome! Feel free to open an issue or submit a pull request.

---
<div align="center">
  <b>Built with ❤️ by Shishagra Nigam</b><br/>
  <i>Empowering the next generation of job seekers.</i>
</div>
