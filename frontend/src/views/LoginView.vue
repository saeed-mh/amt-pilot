<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import { loginUser } from '@/services/auth'

const router = useRouter()

const email = ref('')
const password = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')
const fieldErrors = ref({})

async function handleSubmit() {
  isSubmitting.value = true
  errorMessage.value = ''
  fieldErrors.value = {}

  try {
    const loginResponse = await loginUser(email.value, password.value)

    localStorage.setItem('amtpilot_access_token', loginResponse.accessToken)

    await router.push('/dashboard')
  } catch (error) {
    errorMessage.value = error.message
    fieldErrors.value = error.fieldErrors || {}
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <main class="auth-page">
    <section class="auth-card">
      <div class="brand">AmtPilot</div>

      <h1>Welcome back</h1>
      <p class="subtitle">Log in to continue with your applications.</p>

      <form @submit.prevent="handleSubmit">
        <label for="email">Email</label>
        <input
          id="email"
          v-model="email"
          type="email"
          placeholder="you@example.com"
          autocomplete="email"
          required
        />
        <p v-if="fieldErrors.email" class="field-error">
          {{ fieldErrors.email }}
        </p>

        <label for="password">Password</label>
        <input
          id="password"
          v-model="password"
          type="password"
          placeholder="Your password"
          autocomplete="current-password"
          required
        />
        <p v-if="fieldErrors.password" class="field-error">
          {{ fieldErrors.password }}
        </p>

        <button type="submit" :disabled="isSubmitting">
          {{ isSubmitting ? 'Logging in...' : 'Log in' }}
        </button>
      </form>

      <p v-if="errorMessage" class="message error" role="alert">
        {{ errorMessage }}
      </p>

      <p class="signup-link">
        Don't have an account?
        <RouterLink to="/signup">Create one</RouterLink>
      </p>
    </section>
  </main>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background: #f4f7fb;
  color: #1f2937;
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: 36px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  box-shadow: 0 12px 30px rgb(15 23 42 / 8%);
}

.brand {
  margin-bottom: 24px;
  color: #2563eb;
  font-size: 20px;
  font-weight: 700;
}

h1 {
  margin: 0;
  font-size: 28px;
}

.subtitle {
  margin: 8px 0 28px;
  color: #6b7280;
  line-height: 1.5;
}

form {
  display: grid;
  gap: 10px;
}

label {
  margin-top: 8px;
  font-size: 14px;
  font-weight: 600;
}

input {
  padding: 12px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font: inherit;
}

input:focus {
  border-color: #2563eb;
  outline: 3px solid rgb(37 99 235 / 12%);
}

button {
  margin-top: 16px;
  padding: 12px;
  border: 0;
  border-radius: 8px;
  background: #2563eb;
  color: #ffffff;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

button:hover {
  background: #1d4ed8;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.field-error {
  margin: 0;
  color: #b91c1c;
  font-size: 13px;
}

.message {
  margin: 18px 0 0;
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
}

.error {
  background: #fef2f2;
  color: #b91c1c;
}

.signup-link {
  margin: 24px 0 0;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

a {
  color: #2563eb;
  font-weight: 600;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}
</style>
