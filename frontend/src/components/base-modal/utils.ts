import {computed, type CSSProperties} from "vue";

export const actionClasses = computed<CSSProperties>(() => ({
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'space-between',
}))