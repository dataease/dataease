import type { RouteRecordRaw } from 'vue-router'
import { defineComponent } from 'vue'

declare module 'vue-router' {
  interface RouteMeta extends Record<string | number | symbol, unknown> {
    title?: string
    icon?: string
    hidden?: boolean
  }
}

interface RouteMeta extends Record<string | number | symbol, unknown> {
  title?: string
  icon?: string
  hidden?: boolean
}

type Component<T = any> =
  | ReturnType<typeof defineComponent>
  | (() => Promise<typeof import('*.vue')>)
  | (() => Promise<T>)

declare global {
  interface AppRouteRecordRaw extends Omit<RouteRecordRaw, 'meta' | 'children'> {
    name?: string
    hidden?: boolean
    id?: string
    type?: number
    isPlugin?: boolean
    noLayout?: boolean
    permission?: string
    pid?: number | string
    meta: RouteMeta
    component?: Component | string
    children?: AppRouteRecordRaw[]
    props?: Recordable
    fullPath?: string
  }

  interface AppCustomRouteRecordRaw extends Omit<RouteRecordRaw, 'meta' | 'children'> {
    name?: string
    hidden: boolean
    id: string
    type?: number
    isPlugin?: boolean
    noLayout?: boolean
    permission?: string
    pid?: number | string
    meta?: RouteMeta
    component?: Component | string
    path: string
    redirect?: string
    children?: AppCustomRouteRecordRaw[]
  }
}
