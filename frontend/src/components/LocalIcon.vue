<template>
  <svg
    class="local-icon"
    :width="iconWidth"
    :height="iconHeight"
    viewBox="0 0 24 24"
    aria-hidden="true"
    focusable="false"
    v-html="iconBody"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { localIconPaths, normalizeIconName } from '@/icons/local-icons'

const props = defineProps<{
  name: string
  width?: string | number
  height?: string | number
}>()

const iconWidth = computed(() => props.width ?? '1em')
const iconHeight = computed(() => props.height ?? props.width ?? '1em')
const iconBody = computed(() => localIconPaths[normalizeIconName(props.name)] || localIconPaths['question-circle'])
</script>

<style scoped>
.local-icon {
  display: inline-block;
  flex-shrink: 0;
  line-height: 1;
  vertical-align: -0.125em;
}
</style>
