<script setup lang="ts">
import type {IPageResponse} from "/@/api/types.ts";
import {nextTick, ref, type Ref, watch} from "vue";
import useContainerShadow from "/@/hook/use-container-shadow.ts";

const props = withDefaults(
    defineProps<{
      mode?: 'static' | 'remote'; // 模式，静态数据模式或者远程数据模式，默认静态数据模式，静态数据模式下，需要传入list；远程数据模式下，需要传入api请求函数
      list?: any[];
      cardMinWidth: number; // 卡片最小宽度px
      shadowLimit: number; // 滚动距离高度，用于计算顶部底部阴影
      remoteParams?: Record<string, any>; // 远程数据模式下，请求数据的参数
      gap?: number; // 卡片之间的间距
      isProportional?: boolean; // 是否等比正方形
      paddingBottomSpace?: string; // 是否存在底部的间距
      remoteFunc?: (v: any) => Promise<IPageResponse<any>>; // 远程数据模式下，请求数据的函数
    }>(),
    {
      mode: 'static',
      gap: 24,
      isProportional: true,
    }
);
const topLoading = ref(false);
const remoteList = ref<any[]>([]);
const bottomLoading = ref(false);
const msCardListRef: Ref<HTMLElement | null> = ref(null);
const msCardListContainerRef: Ref<HTMLElement | null> = ref(null);
const {isInitListener, containerStatusClass, setContainer, initScrollListener} =
    useContainerShadow({
      overHeight: props.shadowLimit,
      containerClassName: 'ms-card-list-container',
    });

const initListListener = (arr?: any[]) => {
  if (arr && arr.length > 0 && !isInitListener.value) {
    nextTick(() => {
      if (msCardListRef.value) {
        setContainer(msCardListRef.value);
        initScrollListener();
      }
    });
  }
}
watch(
    () => props.list,
    (val) => {
      if (props.mode === 'static') {
        initListListener(val);
      }
    },
    {
      immediate: true,
    }
);
</script>

<template>
  <div ref="msCardListContainerRef" :class="['ms-card-list-container', containerStatusClass]">
    <div
        ref="msCardListRef"
        class="ms-card-list"
        :style="{
        'grid-template-columns': `repeat(auto-fill, minmax(${props.cardMinWidth}px, 1fr))`,
        'gap': `${props.gap}px` || '24px',
        'padding-bottom': props.paddingBottomSpace || 0,
      }"
    >
      <div v-if="topLoading" class="ms-card-list-loading">
        <n-spin :loading="topLoading"></n-spin>
      </div>
      <div
          v-for="(item, index) in props.mode === 'remote' ? remoteList : props.list"
          :key="item.key"
          class="ms-card-list-item"
          :style="{ 'aspect-ratio': props.isProportional ? 1 : 'none' }"
      >
        <slot name="item" :item="item" :index="index"></slot>
      </div>
      <div v-if="bottomLoading" class="ms-card-list-loading">
        <n-spin :loading="bottomLoading"></n-spin>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ms-card-list-container {
  height: 100%;
  overflow: hidden;
}

.ms-card-list {
  display: grid;
  max-height: 100%;
  overflow: auto;
}


.ms-card-list {
  grid-template-columns: repeat(auto-fill, minmax(102px, 1fr));
}

.ms-card-list .ms-card-list-item {
  position: relative;
  width: 100%;
}

.ms-card-list .ms-card-list-loading {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
}
</style>