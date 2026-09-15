<script setup>
import { computed, nextTick, ref, watch } from 'vue'

import { t } from '@/i18n'

const emit = defineEmits(['cancel', 'confirm'])

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    required: true,
  },
  itemLabel: {
    type: String,
    default: 'Selected item',
  },
  itemName: {
    type: String,
    default: '',
  },
  confirmLabel: {
    type: String,
    required: true,
  },
  pendingLabel: {
    type: String,
    default: 'Working...',
  },
  pending: {
    type: Boolean,
    default: false,
  },
  tone: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'danger'].includes(value),
  },
  icon: {
    type: String,
    default: '+',
  },
})

const dialog = ref(null)
const confirmationLabel = computed(() => (props.pending ? props.pendingLabel : props.confirmLabel))

function cancel() {
  if (!props.pending) {
    emit('cancel')
  }
}

watch(
  () => props.open,
  async (open) => {
    if (open) {
      await nextTick()
      dialog.value?.focus()
    }
  },
)
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="confirmation-backdrop" role="presentation" @click.self="cancel">
      <section
        ref="dialog"
        class="confirmation-card"
        :class="`confirmation-card--${tone}`"
        role="dialog"
        aria-modal="true"
        aria-labelledby="action-confirmation-title"
        aria-describedby="action-confirmation-description"
        tabindex="-1"
        @keydown.esc="cancel"
      >
        <div class="confirmation-icon" aria-hidden="true">{{ icon }}</div>

        <h3 id="action-confirmation-title">{{ title }}</h3>
        <p id="action-confirmation-description">{{ description }}</p>

        <div class="confirmation-item">
          <span>{{ itemLabel }}</span>
          <strong>{{ itemName }}</strong>
        </div>

        <div class="confirmation-actions">
          <button class="cancel-button" type="button" :disabled="pending" @click="cancel">
            {{ t('common.cancel') }}
          </button>
          <button
            class="confirm-button"
            type="button"
            :disabled="pending"
            @click="$emit('confirm')"
          >
            {{ confirmationLabel }}
          </button>
        </div>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.confirmation-backdrop {
  position: fixed;
  z-index: 1000;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 1.25rem;
  background: rgb(15 23 42 / 48%);
  backdrop-filter: blur(2px);
}

.confirmation-card {
  width: min(100%, 28rem);
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.875rem;
  background: #ffffff;
  box-shadow: 0 24px 60px rgb(15 23 42 / 22%);
  outline: none;
}

.confirmation-icon {
  display: grid;
  width: 2.75rem;
  height: 2.75rem;
  place-items: center;
  border-radius: 0.75rem;
  background: #eff6ff;
  color: #2563eb;
  font-size: 1.5rem;
  font-weight: 600;
}

.confirmation-card--danger .confirmation-icon {
  background: #fef2f2;
  color: #dc2626;
}

.confirmation-card h3 {
  margin: 1rem 0 0.5rem;
  color: #1f2937;
  font-size: 1.25rem;
}

.confirmation-card > p {
  margin: 0;
  color: #64748b;
  line-height: 1.5;
}

.confirmation-item {
  margin-top: 1.25rem;
  padding: 0.875rem 1rem;
  border: 1px solid #dbeafe;
  border-radius: 0.625rem;
  background: #f8fbff;
}

.confirmation-card--danger .confirmation-item {
  border-color: #fecaca;
  background: #fffafa;
}

.confirmation-item span,
.confirmation-item strong {
  display: block;
}

.confirmation-item span {
  margin-bottom: 0.25rem;
  color: #64748b;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.confirmation-item strong {
  color: #1f2937;
}

.confirmation-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.625rem;
  margin-top: 1.5rem;
}

.cancel-button,
.confirm-button {
  padding: 0.625rem 1rem;
  border-radius: 0.5rem;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.cancel-button {
  border: 1px solid #cbd5e1;
  background: #ffffff;
  color: #475569;
}

.cancel-button:hover:not(:disabled) {
  background: #f8fafc;
}

.confirm-button {
  border: 1px solid #2563eb;
  background: #2563eb;
  color: #ffffff;
}

.confirmation-card--danger .confirm-button {
  border-color: #dc2626;
  background: #dc2626;
}

.confirm-button:hover:not(:disabled) {
  background: #1d4ed8;
}

.confirmation-card--danger .confirm-button:hover:not(:disabled) {
  background: #b91c1c;
}

.cancel-button:disabled,
.confirm-button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

@media (max-width: 600px) {
  .confirmation-actions {
    flex-direction: column-reverse;
  }

  .cancel-button,
  .confirm-button {
    width: 100%;
  }
}
</style>
