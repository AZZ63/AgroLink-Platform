<template>
  <div class="menu-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">菜单管理</h1>
        <p class="page-desc">管理后台导航菜单，支持多级树形结构</p>
      </div>
      <button class="btn btn-primary" @click="openDialog(null)">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增根菜单
      </button>
    </div>

    <!-- 表格卡片 -->
    <div class="table-card">
      <div class="table-container">
        <table class="menu-table">
          <thead>
            <tr>
              <th class="col-sort">排序</th>
              <th class="col-name">菜单名称</th>
              <th class="col-path">路由路径</th>
              <th class="col-component">组件</th>
              <th class="col-icon">图标</th>
              <th class="col-perm">权限标识</th>
              <th class="col-visible">状态</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <template v-for="item in menuTree" :key="item.id">
              <!-- 父行 -->
              <tr class="tr-parent">
                <td class="td-sort">{{ item.sortOrder }}</td>
                <td class="td-name">
                  <div class="parent-label">{{ item.menuName }}</div>
                </td>
                <td class="td-path"><code class="code-path">{{ item.path || '—' }}</code></td>
                <td class="td-component"><code class="code-component">{{ item.component || '—' }}</code></td>
                <td class="td-icon"><code class="code-icon">{{ item.icon || '—' }}</code></td>
                <td class="td-perm"><code class="code-perm">{{ item.permissionCode || '—' }}</code></td>
                <td class="td-visible"><span :class="['tag-badge', item.visible ? 'tag-success' : 'tag-default']">{{ item.visible ? '显示' : '隐藏' }}</span></td>
                <td class="td-actions">
                  <button class="action-btn" @click="openDialog(item.id)" title="添加子菜单">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                  </button>
                  <button class="action-btn" @click="openDialog(item)" title="编辑">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                  <button class="action-btn action-btn-danger" @click="handleDelete(item)" title="删除">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                  </button>
                </td>
              </tr>
              <!-- 子行 -->
              <tr v-for="child in item.children" :key="child.id" class="tr-child">
                <td class="td-sort">{{ child.sortOrder }}</td>
                <td class="td-name">
                  <div class="child-label">
                    <span class="tree-line">└</span>
                    {{ child.menuName }}
                  </div>
                </td>
                <td class="td-path"><code class="code-path">{{ child.path || '—' }}</code></td>
                <td class="td-component"><code class="code-component">{{ child.component || '—' }}</code></td>
                <td class="td-icon"><code class="code-icon">{{ child.icon || '—' }}</code></td>
                <td class="td-perm"><code class="code-perm">{{ child.permissionCode || '—' }}</code></td>
                <td class="td-visible"><span :class="['tag-badge', child.visible ? 'tag-success' : 'tag-default']">{{ child.visible ? '显示' : '隐藏' }}</span></td>
                <td class="td-actions">
                  <button class="action-btn" @click="openDialog(child)" title="编辑">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                  <button class="action-btn action-btn-danger" @click="handleDelete(child)" title="删除">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                  </button>
                </td>
              </tr>
            </template>
            <tr v-if="menuTree.length === 0">
              <td colspan="8" class="td-empty">暂无菜单数据，点击右上角「新增根菜单」创建</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <div v-if="showDialog" class="modal-overlay" @click.self="showDialog = false">
      <div class="modal-card">
        <div class="modal-header">
          <h2 class="modal-title">{{ editingId ? '编辑菜单' : '新增菜单' }}</h2>
          <button class="modal-close" @click="showDialog = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-grid">
            <div class="form-group">
              <label>菜单名称</label>
              <input v-model="form.menuName" class="form-input" placeholder="如：用户管理" />
            </div>
            <div class="form-group">
              <label>父菜单ID</label>
              <input v-model.number="form.parentId" class="form-input" type="number" placeholder="0=顶级菜单" />
            </div>
            <div class="form-group">
              <label>路由路径</label>
              <input v-model="form.path" class="form-input" placeholder="/admin/users" />
            </div>
            <div class="form-group">
              <label>组件路径</label>
              <input v-model="form.component" class="form-input" placeholder="admin/UserManagement" />
            </div>
            <div class="form-group">
              <label>图标</label>
              <input v-model="form.icon" class="form-input" placeholder="User" />
            </div>
            <div class="form-group">
              <label>权限标识</label>
              <input v-model="form.permissionCode" class="form-input" placeholder="user:list" />
            </div>
            <div class="form-group">
              <label>排序号</label>
              <input v-model.number="form.sortOrder" class="form-input" type="number" min="0" placeholder="0" />
            </div>
            <div class="form-group">
              <label>是否显示</label>
              <div class="switch-group">
                <button :class="['switch-btn', { active: form.visible === 1 }]" @click="form.visible = 1">显示</button>
                <button :class="['switch-btn', { active: form.visible === 0 }]" @click="form.visible = 0">隐藏</button>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-cancel" @click="showDialog = false">取消</button>
          <button class="btn btn-primary" @click="handleSave" :disabled="saving">
            {{ saving ? '保存中...' : editingId ? '保存修改' : '确认创建' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api/http'

const menuTree = ref([])
const showDialog = ref(false)
const editingId = ref(null)
const saving = ref(false)
const form = ref({ parentId: 0, menuName: '', path: '', component: '', icon: '', permissionCode: '', sortOrder: 0, visible: 1 })

async function loadMenus() {
  try { menuTree.value = await adminApi.getMenus() }
  catch { ElMessage.error('加载菜单失败') }
}

function openDialog(arg) {
  if (arg === null || typeof arg === 'number') {
    editingId.value = null
    form.value = { parentId: arg || 0, menuName: '', path: '', component: '', icon: '', permissionCode: '', sortOrder: 0, visible: 1 }
  } else {
    editingId.value = arg.id
    form.value = {
      parentId: arg.parentId || 0,
      menuName: arg.menuName,
      path: arg.path || '',
      component: arg.component || '',
      icon: arg.icon || '',
      permissionCode: arg.permissionCode || '',
      sortOrder: arg.sortOrder ?? 0,
      visible: arg.visible ?? 1
    }
  }
  showDialog.value = true
}

async function handleSave() {
  if (!form.value.menuName) return ElMessage.warning('请输入菜单名称')
  saving.value = true
  try {
    const payload = { ...form.value }
    if (editingId.value) { await adminApi.updateMenu(editingId.value, payload); ElMessage.success('菜单已更新') }
    else { await adminApi.createMenu(payload); ElMessage.success('菜单已创建') }
    showDialog.value = false; loadMenus()
  } catch (e) { ElMessage.error(e.message || '操作失败') }
  finally { saving.value = false }
}

async function handleDelete(item) {
  try {
    await ElMessageBox.confirm(`确定删除「${item.menuName}」？`, '确认删除', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    await adminApi.deleteMenu(item.id); ElMessage.success('已删除'); loadMenus()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.message) }
}

onMounted(loadMenus)
</script>

<style scoped>
/* ===== 页面布局 ===== */
.menu-page { max-width: 1200px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.page-title { font-size: 22px; font-weight: 700; color: #111827; margin: 0 0 4px; letter-spacing: -0.3px; }
.page-desc { font-size: 14px; color: #6b7280; margin: 0; }

/* ===== 表格卡片 ===== */
.table-card {
  background: #ffffff; border: 1px solid #e5e7eb; border-radius: 16px;
  overflow: hidden; box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}
.table-container { overflow-x: auto; }

/* ===== 表格 ===== */
.menu-table { width: 100%; border-collapse: collapse; font-size: 13px; }

/* 列宽 */
.col-sort { width: 56px; }
.col-name { min-width: 140px; }
.col-path { min-width: 140px; }
.col-component { min-width: 160px; }
.col-icon { width: 80px; }
.col-perm { min-width: 120px; }
.col-visible { width: 72px; }
.col-actions { width: 110px; }

/* 表头 */
.menu-table thead th {
  text-align: left; padding: 14px 16px;
  background: #f9fafb; color: #374151; font-size: 12px; font-weight: 600;
  letter-spacing: 0.3px; border-bottom: 1px solid #e5e7eb; white-space: nowrap;
}

/* 表格行 */
.menu-table tbody td {
  padding: 14px 16px; border-bottom: 1px solid #f3f4f6;
  color: #374151; line-height: 1.5; vertical-align: middle;
}
.menu-table tbody tr:last-child td { border-bottom: none; }

/* 父行背景 */
.tr-parent td { background: #fafbfc; }

/* 子行缩进 */
.child-label { display: flex; align-items: center; gap: 6px; padding-left: 8px; }
.tree-line { color: #d1d5db; font-size: 12px; font-family: 'JetBrains Mono', monospace; }

/* 排序号 */
.td-sort { font-family: 'JetBrains Mono', monospace; font-size: 12px; color: #6b7280; text-align: center; }

/* 名称 */
.parent-label { font-weight: 600; color: #111827; }

/* 代码样式 */
.code-path, .code-component { font-family: 'JetBrains Mono', monospace; font-size: 12px; color: #6b7280; background: #f3f4f6; padding: 2px 8px; border-radius: 4px; white-space: nowrap; }
.code-icon { font-family: 'JetBrains Mono', monospace; font-size: 12px; color: #6b7280; }
.code-perm { font-family: 'JetBrains Mono', monospace; font-size: 11px; color: #7CCF5F; background: #f0fdf4; padding: 2px 8px; border-radius: 4px; }

/* ===== 操作按钮 ===== */
.td-actions { white-space: nowrap; }
.action-btn {
  display: inline-flex; align-items: center; justify-content: center;
  width: 30px; height: 30px; border: 1px solid #e5e7eb; border-radius: 7px;
  background: #ffffff; color: #6b7280; cursor: pointer;
  transition: all 0.15s; margin-right: 4px;
}
.action-btn:hover { border-color: #7CCF5F; color: #7CCF5F; background: #f0fdf4; }
.action-btn-danger:hover { border-color: #ef4444; color: #ef4444; background: #fef2f2; }

/* ===== 按钮 ===== */
.btn {
  display: inline-flex; align-items: center; gap: 6px;
  font-family: 'Inter', sans-serif; font-weight: 500; cursor: pointer;
  border-radius: 9px; transition: all 0.15s; border: none; font-size: 13px;
}
.btn-primary { background: #7CCF5F; color: #fff; padding: 10px 20px; }
.btn-primary:hover { background: #6bc04e; }
.btn-primary:disabled { opacity: 0.5; cursor: default; }
.btn-cancel { background: #fff; color: #374151; border: 1px solid #e5e7eb; padding: 10px 24px; }
.btn-cancel:hover { background: #f9fafb; }

/* ===== 空状态 ===== */
.td-empty { text-align: center; color: #9ca3af; padding: 48px 16px !important; font-size: 13px; }

/* ===== 弹窗 ===== */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.35); display: flex; align-items: center; justify-content: center; z-index: 100; padding: 24px; }
.modal-card { background: #fff; border-radius: 20px; width: 560px; max-width: 100%; box-shadow: 0 20px 60px rgba(0,0,0,0.12); }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 24px 28px 0; }
.modal-title { font-size: 17px; font-weight: 600; color: #111827; margin: 0; }
.modal-close { background: none; border: none; color: #9ca3af; font-size: 18px; cursor: pointer; width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.modal-close:hover { background: #f3f4f6; color: #374151; }
.modal-body { padding: 20px 28px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 0 28px 24px; }

/* ===== 表单 ===== */
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 13px; font-weight: 500; color: #374151; }
.form-input {
  padding: 9px 12px; border: 1px solid #e5e7eb; border-radius: 9px;
  font-size: 13px; color: #111827; background: #fff; outline: none; box-sizing: border-box;
  font-family: 'Inter', sans-serif; width: 100%; transition: border-color 0.15s;
}
.form-input:focus { border-color: #7CCF5F; box-shadow: 0 0 0 3px rgba(124,207,95,0.08); }
.form-input::placeholder { color: #9ca3af; }

/* 开关按钮 */
.switch-group { display: flex; gap: 4px; background: #f3f4f6; border-radius: 8px; padding: 3px; }
.switch-btn {
  flex: 1; padding: 7px 16px; border: none; border-radius: 6px;
  font-size: 13px; font-weight: 500; cursor: pointer; font-family: 'Inter', sans-serif;
  background: transparent; color: #6b7280; transition: all 0.15s;
}
.switch-btn.active { background: #fff; color: #111827; box-shadow: 0 1px 2px rgba(0,0,0,0.06); }

/* ===== 标签 ===== */
.tag-badge { display: inline-flex; align-items: center; padding: 3px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; line-height: 1.4; }
.tag-success { background: #f0fdf4; color: #16a34a; }
.tag-default { background: #f3f4f6; color: #6b7280; }
</style>
