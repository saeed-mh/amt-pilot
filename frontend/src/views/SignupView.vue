<script setup>
import { ref } from 'vue'

import { registerUser } from '@/services/auth'

const email = ref('')
const password = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const fieldErrors = ref({})

async function handleSubmit() {
  isSubmitting.value = true
  errorMessage.value = ''
  successMessage.value = ''
  fieldErrors.value = {}

  try {
    await registerUser(email.value, password.value)

    successMessage.value = 'Account created successfully. You can now log in.'

    password.value = ''
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

      <h1>Create your account</h1>
      <p class="subtitle">Start organizing your administrative processes.</p>

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
          placeholder="At least 8 characters"
          autocomplete="new-password"
          minlength="8"
          required
        />
        <p v-if="fieldErrors.password" class="field-error">
          {{ fieldErrors.password }}
        </p>

        <button type="submit" :disabled="isSubmitting">
          {{ isSubmitting ? 'Creating account...' : 'Create account' }}
        </button>
      </form>

      <p v-if="errorMessage" class="message error" role="alert">
        {{ errorMessage }}
      </p>

      <p v-if="successMessage" class="message success" role="status">
        {{ successMessage }}
      </p>

      <p class="login-link">
        Already have an account?
        <RouterLink to="/login">Log in</RouterLink>
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

.success {
  background: #ecfdf5;
  color: #047857;
}

.login-link {
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
