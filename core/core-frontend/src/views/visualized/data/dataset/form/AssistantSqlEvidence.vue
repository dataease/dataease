<script setup lang="ts">
import { computed } from 'vue'
import type { AiSqlTableEvidence } from '@/api/aiConversation'
import { useI18n } from '@/hooks/web/useI18n'

const props = defineProps<{ tables?: AiSqlTableEvidence[] | null }>()
const { t } = useI18n()
const tr = (key: string) => t(`ai_conversation.${key}`)
const sources = computed(() =>
  (props.tables ?? []).map(table => ({
    ...table,
    hasMeanings: table.fields.some(
      field => field.businessName || field.description || field.values?.length
    )
  }))
)
</script>

<template>
  <details class="sql-evidence">
    <summary>{{ tr('sql_evidence') }}</summary>
    <p v-if="!sources.length" class="empty">{{ tr('no_sql_evidence') }}</p>
    <section v-for="source in sources" :key="source.table" class="evidence-table">
      <header>
        <div class="table-title">
          <code>{{ source.table }}</code>
          <small>{{ tr(source.inspected ? 'verified_structure' : 'sql_reference') }}</small>
        </div>
        <p v-if="source.description">{{ source.description }}</p>
        <p v-if="source.grain">{{ source.grain }}</p>
      </header>
      <div v-for="relation in source.relations" :key="JSON.stringify(relation)" class="relation">
        <p>{{ tr('confirmed_join') }} · {{ relation.name }} · {{ relation.cardinality }}</p>
        <code>{{ relation.leftTable }} {{ relation.joinType }} JOIN {{ relation.rightTable }}</code>
        <p v-if="relation.actual">
          {{ tr('actual_join') }} ·
          <code
            >{{ relation.actual.leftTable }} {{ relation.actual.joinType }} JOIN
            {{ relation.actual.rightTable }}</code
          >
        </p>
        <code v-for="pair in relation.conditions" :key="pair.leftField + '=' + pair.rightField">
          {{ relation.leftTable }}.{{ pair.leftField }} = {{ relation.rightTable }}.{{
            pair.rightField
          }}
        </code>
      </div>
      <div v-if="source.fields.length" class="fields">
        <table :aria-label="source.table">
          <colgroup v-if="source.hasMeanings">
            <col class="name-col" />
            <col class="type-col" />
            <col />
          </colgroup>
          <thead>
            <tr>
              <th scope="col">{{ t('datasource.column_name') }}</th>
              <th scope="col">{{ t('datasource.field_type') }}</th>
              <th v-if="source.hasMeanings" scope="col">{{ tr('confirmed_meaning') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="field in source.fields" :key="field.name">
              <th scope="row">
                <code>{{ field.name }}</code>
                <span
                  v-if="
                    field.comment &&
                    field.comment !== field.name &&
                    field.comment !== field.businessName
                  "
                  class="field-note"
                >
                  {{ field.comment }}
                </span>
              </th>
              <td>
                <code class="field-type">{{ field.type }}</code>
              </td>
              <td v-if="source.hasMeanings">
                <span v-if="field.businessName">{{ field.businessName }}</span>
                <span
                  v-if="field.description && field.description !== field.businessName"
                  class="field-note"
                  >{{ field.description }}</span
                >
                <span v-for="value in field.values" :key="value.value" class="field-note"
                  ><code>{{ value.value }}</code> → {{ value.meaning }}</span
                >
                <span
                  v-if="!field.businessName && !field.description && !field.values?.length"
                  class="field-note"
                  >—</span
                >
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </details>
</template>

<style scoped lang="less">
.sql-evidence {
  min-width: 0;
  font-size: 13px;
  line-height: 20px;
  overflow-wrap: anywhere;

  p {
    margin: 0;
  }
  .empty {
    padding: 6px 0;
    color: var(--ed-text-color-regular);
  }
}
.evidence-table {
  margin: 6px 0;
  overflow: hidden;
  border: 1px solid var(--ed-border-color);
  border-radius: 6px;
  header,
  .relation {
    padding: 7px 10px;
  }
  header {
    background: var(--ed-fill-color-light);
  }
  header p {
    margin-top: 2px;
    color: var(--ed-text-color-regular);
  }
}
.table-title {
  display: flex;
  flex-wrap: wrap;
  gap: 2px 10px;
  align-items: baseline;
  code {
    font-weight: 600;
  }
}
.relation {
  display: grid;
  gap: 2px;
  border-top: 1px solid var(--ed-border-color);
}
.fields {
  max-height: min(340px, 45vh);
  overflow: auto;
  border-top: 1px solid var(--ed-border-color);
  table {
    width: 100%;
    table-layout: fixed;
    border-collapse: separate;
    border-spacing: 0;
    font: inherit;
    text-align: start;
  }
  .name-col {
    width: 32%;
  }
  .type-col {
    width: 22%;
  }
  td,
  th {
    padding: 5px 8px;
    vertical-align: top;
    text-align: start;
    font-weight: normal;
  }
  thead th {
    position: sticky;
    top: 0;
    color: var(--ed-text-color-regular);
    font-size: 12px;
    background: var(--ed-bg-color);
    border-bottom: 1px solid var(--ed-border-color);
  }
  tbody tr + tr > * {
    border-top: 1px solid var(--ed-border-color-lighter);
  }
  .field-note {
    display: block;
    color: var(--ed-text-color-regular);
    font-size: 12px;
    line-height: 18px;
  }
  .field-type {
    color: var(--ed-text-color-regular);
    font-size: 12px;
  }
}
</style>
