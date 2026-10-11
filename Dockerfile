FROM python:3.11-slim

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PYTHONPATH=/app/backend \
    PORT=7860

# Install system utilities if needed
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Set up user for Hugging Face Spaces (UID 1000)
RUN useradd -m -u 1000 user
ENV HOME=/home/user \
    PATH=/home/user/.local/bin:$PATH

# Install python dependencies
COPY --chown=user:user backend/requirements.txt /app/backend/requirements.txt
RUN pip install --no-cache-dir -r /app/backend/requirements.txt

# Copy backend code
COPY --chown=user:user backend /app/backend

# Switch to non-root user
USER user

WORKDIR /app/backend

EXPOSE 7860

CMD ["sh", "-c", "uvicorn api.app.main:app --host 0.0.0.0 --port ${PORT:-7860}"]
