# Test script for Groq API
# Usage: ./test-groq-api.sh

API_KEY="$GROQ_API_KEY"
MODEL="llama-3.1-70b-versatile"
URL="https://api.groq.com/openai/v1/chat/completions"

echo "Testing Groq API connection..."
echo "API Key: ${API_KEY:0:10}..."
echo "Model: $MODEL"
echo ""

curl -X POST "$URL" \
  -H "Authorization: Bearer $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "'"$MODEL"'",
    "messages": [
      {"role": "system", "content": "You are a helpful assistant."},
      {"role": "user", "content": "Say hello"}
    ],
    "temperature": 0.3
  }' \
  -w "\n\nHTTP Status: %{http_code}\n" \
  -s
