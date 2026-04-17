from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware
import os
from dotenv import load_dotenv
from google import genai
import pypdf
import io

load_dotenv()

from auth import auth_router

app = FastAPI(title="HireMate AI Backend")

# Include the new authentication routes
app.include_router(auth_router)

# Setup CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class ChatRequest(BaseModel):
    message: str
    modelChoice: str = "gemini"  # "gemini" is the default now (free!)

# ── Configure Google Gemini (FREE tier) ──────────────────────────────────
gemini_api_key = os.environ.get("GEMINI_API_KEY", "").strip()
client = None

SYSTEM_INSTRUCTION = (
    "You are HireMate, an expert AI career guidance counselor. "
    "Help the user with job searching, resume optimization, interview prep, and career advice. "
    "Keep responses professional, encouraging, and detailed. "
    "Use bullet points and clear formatting when providing lists or steps. "
    "Always be supportive and motivating."
)

if gemini_api_key:
    client = genai.Client(api_key=gemini_api_key)
    print("[OK] Google Gemini client initialized (using gemini-2.0-flash - FREE tier)")
else:
    print("[WARNING] No GEMINI_API_KEY found in .env - chatbot will not work.")
    print("[INFO] Get your FREE API key at: https://aistudio.google.com/app/apikey")

# Store conversation history per session (in-memory for simplicity)
chat_sessions = {}

@app.get("/")
def read_root():
    return {
        "status": "HireMate API is running",
        "gemini_configured": client is not None,
        "model": "gemini-2.5-flash (FREE)",
    }

@app.post("/api/chat")
async def chat_endpoint(
    message: str = Form(""),
    modelChoice: str = Form("gemini"),
    enableSearch: str = Form("false"),
    systemContext: str = Form(""),
    file: UploadFile = File(None)
):
    try:
        if not client:
            return {
                "reply": (
                    "⚠️ Gemini API key is not configured. "
                    "Please add your FREE GEMINI_API_KEY to the backend/.env file and restart the server.\n\n"
                    "Get your free key at: https://aistudio.google.com/app/apikey"
                )
            }

        file_text = ""
        if file and file.filename.endswith('.pdf'):
            try:
                pdf_reader = pypdf.PdfReader(io.BytesIO(await file.read()))
                for page in pdf_reader.pages:
                    extracted = page.extract_text()
                    if extracted:
                        file_text += extracted + "\n"
            except Exception as e:
                print(f"[ERROR] Failed to read PDF: {e}")
                return {"reply": f"Sorry, I couldn't read the uploaded PDF: {e}"}

        prompt_contents = message
        if file_text:
            prompt_contents += f"\n\n--- EXTRACTED RESUME TEXT ---\n{file_text}"
        
        if not prompt_contents.strip():
            return {"reply": "Please provide a message or upload a file."}

        instruction = SYSTEM_INSTRUCTION
        if systemContext:
            instruction += f"\n\nContext for this session (VERY IMPORTANT): {systemContext}"

        config_kwargs = {
            "system_instruction": instruction,
        }
        if enableSearch.lower() == "true":
            config_kwargs["tools"] = [{"google_search": {}}]

        # Use Gemini to generate a response
        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=prompt_contents,
            config=genai.types.GenerateContentConfig(**config_kwargs),
        )
        reply = response.text

        return {"reply": reply}

    except Exception as e:
        error_msg = str(e)
        print(f"[ERROR] Gemini request failed: {error_msg}")

        if "API_KEY_INVALID" in error_msg or "invalid" in error_msg.lower():
            return {"reply": "❌ Invalid Gemini API key. Please check your GEMINI_API_KEY in the .env file."}
        elif "quota" in error_msg.lower() or "429" in error_msg:
            return {"reply": "⚠️ Your API key is returning a 'Quota Exceeded' error. If you just created it, please note that Google's Free Tier is not available in some regions (like the EU/UK) without setting up billing, which gives a limit of 0 tokens. Please check your Google AI studio billing/region!"}
        elif "blocked" in error_msg.lower() or "safety" in error_msg.lower():
            return {"reply": "⚠️ The response was blocked by safety filters. Please try rephrasing your question."}
        else:
            return {"reply": f"Sorry, there was an error: {error_msg}"}


if __name__ == "__main__":
    import uvicorn
    # Use the PORT environment variable if available (Railway/Render/Heroku set this)
    port = int(os.environ.get("PORT", 8000))
    # In production, we must listen on 0.0.0.0 to be accessible externally
    uvicorn.run(app, host="0.0.0.0", port=port)
