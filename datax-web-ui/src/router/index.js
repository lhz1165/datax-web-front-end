import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/* Router Modules */
// import componentsRouter from './modules/components'
// import chartsRouter from './modules/charts'
// import tableRouter from './modules/table'
// import nestedRouter from './modules/nested'

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'             the icon show in the sidebar
    noCache: true                if set true, the page will no be cached(default is false)
    affix: true                  if set true, the tag will affix in the tags-view
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path*',
        component: () => import('@/views/redirect/index')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },
  {
    path: '/auth-redirect',
    component: () => import('@/views/login/auth-redirect'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error-page/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error-page/401'),
    hidden: true
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/dashboard/admin/index'),
        name: 'Dashboard',
        meta: { title: '运行报表', icon: 'dashboard', affix: true }
      }
    ]
  }
]

/**
 * asyncRoutes
 * the routes that need to be dynamically loaded based on user roles
 */
export const asyncRoutes = [
  {
    path: '/profile',
    component: Layout,
    redirect: '/profile/index',
    hidden: true,
    children: [
      {
        path: 'index',
        component: () => import('@/views/profile/index'),
        name: 'Profile',
        meta: { title: 'Profile', icon: 'user', noCache: true }
      }
    ]
  },
  {
    path: '/error',
    component: Layout,
    redirect: 'noRedirect',
    name: 'ErrorPages',
    hidden: true,
    meta: {
      title: 'Error Pages',
      icon: '404'
    },
    children: [
      {
        path: '401',
        component: () => import('@/views/error-page/401'),
        name: 'Page401',
        meta: { title: '401', noCache: true }
      },
      {
        path: '404',
        component: () => import('@/views/error-page/404'),
        name: 'Page404',
        meta: { title: '404', noCache: true }
      }
    ]
  },
  {
    path: '/datax/project',
    component: Layout,
    redirect: '/datax/jobProject',
    name: 'datasource',
    meta: { title: '项目管理', icon: 'project' },
    children: [
      {
        path: 'jobProject',
        name: 'jobProject',
        component: () => import('@/views/datax/jobProject/index'),
        meta: { title: '项目管理', icon: 'project' }
      }
    ]
  },
  // 数据集成
  {
    path: '/datax/integration',
    component: Layout,
    redirect: '/datax/integration/datasource',
    name: 'integration',
    meta: { title: '数据集成', icon: 'work' },
    children: [
      {
        path: 'datasource',
        name: 'IntegrationDatasource',
        component: () => import('@/views/datax/jdbc-datasource/index'),
        meta: { title: '数据源管理', icon: 'db_source' }
      },
      {
        path: 'database',
        name: 'IntegrationDatabase',
        component: () => import('@/views/datax/database/index'),
        hidden: true,
        meta: { title: '数据库管理', icon: 'cfg-datasouce' }
      },
      {
        path: 'database/create-table',
        name: 'IntegrationDatabaseCreateTable',
        component: () => import('@/views/datax/database/create-table'),
        hidden: true,
        meta: { title: '创建表', icon: 'table' }
      },
      {
        path: 'database/alter-table',
        name: 'IntegrationDatabaseAlterTable',
        component: () => import('@/views/datax/database/alter-table'),
        hidden: true,
        meta: { title: '修改表', icon: 'edit' }
      },
      {
        path: 'database/create-view',
        name: 'IntegrationDatabaseCreateView',
        component: () => import('@/views/datax/database/create-view'),
        hidden: true,
        meta: { title: '创建视图', icon: 'view' }
      },
      {
        path: 'jobInfo',
        name: 'IntegrationJobInfo',
        component: () => import('@/views/datax/jobInfo/index'),
        meta: { title: '任务管理', icon: 'task-cfg' }
      },
      {
        path: 'apiPublish',
        name: 'IntegrationApiPublish',
        component: () => import('@/views/datax/api-publish/index'),
        meta: { title: 'API发布', icon: 'link' }
      },
      {
        path: 'jsonBuild',
        name: 'IntegrationJsonBuild',
        component: () => import('@/views/datax/json-build/index'),
        hidden: true,
        meta: { title: '任务构建', icon: 'guide', noCache: false }
      },
      {
        path: 'jsonBuildBatch',
        name: 'IntegrationJsonBuildBatch',
        component: () => import('@/views/datax/json-build-batch/index'),
        hidden: true,
        meta: { title: '任务批量构建', icon: 'batch-create', noCache: false }
      },
      {
        path: 'jobTemplate',
        name: 'IntegrationJobTemplate',
        component: () => import('@/views/datax/jobTemplate/index'),
        meta: { title: 'DataX任务模板', icon: 'task-tmp' }
      },
      {
        path: 'jobLog',
        name: 'IntegrationJobLog',
        component: () => import('@/views/datax/jobLog/index'),
        meta: { title: '日志管理', icon: 'log' }
      }
    ]
  },
  // 系统
  {
    path: '/system',
    component: Layout,
    redirect: '/system/user',
    name: 'system',
    meta: { title: '系统', icon: 'system' },
    children: [
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('@/views/datax/user/index'),
        meta: { title: '用户管理', icon: 'table', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'registry',
        name: 'SystemRegistry',
        component: () => import('@/views/datax/registry/index'),
        meta: { title: '资源监控', icon: 'battery-line' }
      },
      {
        path: 'executor',
        name: 'SystemExecutor',
        component: () => import('@/views/datax/executor/index'),
        meta: { title: '执行器管理', icon: 'exe-cfg' }
      },
      {
        path: 'jsonTool',
        name: 'SystemJsonTool',
        component: () => import('@/views/tool/jsonFormat'),
        meta: { title: 'json工具', icon: 'json' }
      }
    ]
  },
  {
    path: '*', redirect: '/404', hidden: true
  }
]

const createRouter = () => new Router({
  // mode: 'history', // require service support
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

const router = createRouter()

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router
