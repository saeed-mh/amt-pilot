<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function logout() {
  localStorage.removeItem('amtpilot_access_token')
  router.push('/login')
}
</script>

<template>
  <main class="dashboard-page">
    <section class="dashboard">
      <header>
        <div class="brand">AmtPilot</div>

        <button type="button" @click="logout">Log out</button>
      </header>

      <div class="welcome">
        <p class="eyebrow">Dashboard</p>
        <h1>Welcome to AmtPilot</h1>
        <p>
          You are logged in. Your processes and applications will appear here as we build the next
          features.
        </p>
      </div>
    </section>
  </main>
</template>

<style scoped>
.dashboard-page {
  min-height: 100vh;
  padding: 24px;
  background: #f4f7fb;
  color: #1f2937;
}

.dashboard {
  width: 100%;
  max-width: 960px;
  margin: 0 auto;
}

header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.brand {
  color: #2563eb;
  font-size: 20px;
  font-weight: 700;
}

button {
  padding: 9px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #ffffff;
  color: #374151;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

button:hover {
  background: #f9fafb;
}

.welcome {
  margin-top: 24px;
  padding: 36px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

h1 {
  margin: 0;
  font-size: 30px;
}

.welcome p:last-child {
  max-width: 600px;
  margin: 12px 0 0;
  color: #6b7280;
  line-height: 1.6;
}

@media (max-width: 600px) {
  .dashboard-page {
    padding: 16px;
  }

  header,
  .welcome {
    padding: 20px;
  }
}
</style>
