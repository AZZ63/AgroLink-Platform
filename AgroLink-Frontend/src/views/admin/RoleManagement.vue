<template>
  <div class="admin-page">
    <div class="page-heading">
      <h1 class="page-title">角色管理</h1>
      <p class="page-subtitle">管理系统角色与权限</p>
      <div class="header-actions">
        <button class="btn btn-primary btn-sm" @click="showRoleDialog = true; editingRole = null; roleForm = { roleCode: '', roleName: '' }">+ 新增角色</button>
      </div>
    </div>

    <div class="split-layout">
      <!-- 左侧：角色列表 -->
      <div class="role-panel">
        <div class="panel-header">角色列表</div>
        <div class="role-list">
          <div v-for="r in roles" :key="r.id"
            :class="['role-card', { active: selectedRole?.id === r.id }]"
            @click="selectRole(r)">
            <div class="rc-top">
              <span class="rc-name">{{ r.roleName }}</span>
              <span class="rc-code">{{ r.roleCode }}</span>
            </div>
            <div class="rc-actions">
              <button class="btn btn-xs btn-ghost" @click.stop="editRole(r)">编辑</button>
              <button v-if="!builtinRoles.includes(r.roleCode)" class="btn btn-xs btn-ghost btn-danger-text" @click.stop="handleDeleteRole(r)">删除</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：权限分配 -->
      <div class="perm-panel">
        <div class="panel-header">
          权限分配
          <span v-if="selectedRole" class="panel-subheader">{{ selectedRole.roleName }}</span>
        </div>

        <div v-if="!selectedRole" class="empty-state">请从左侧选择一个角色</div>

        <template v-else-if="permLoading">
          <div style="text-align:center;padding:40px;color:#9ca3af;">加载中...</div>
        </template>

        <template v-else>
          <div v-for="mod in permTree" :key="mod.id" class="perm-module">
            <div class="perm-module-title">{{ mod.label }}</div>
            <div class="perm-items">
              <label v-for="p in mod.children" :key="p.id" :class="['perm-item', { checked: checkedPerms.has(p.id) }]">
                <input type="checkbox" :checked="checkedPerms.has(p.id)" @change="togglePerm(p.id)" />
                <span>{{ p.label }}</span>
              </label>
            </div>
          </div>
          <div class="perm-actions">
            <button class="btn btn-primary" @click="handleSavePerms" :disabled="saving">
              {{ saving ? '保存中...' : '保存权限' }}
            </button>
          </div>
        </template>
      </div>
    </div>

    <!-- 角色编辑对话框 -->
    <div v-if="showRoleDialog" class="modal-overlay" @click.self="showRoleDialog = false">
      <div class="modal-card" style="width:400px;">
        <h3 class="modal-title">{{ editingRole ? '编辑角色' : '新增角色' }}</h3>
        <div class="modal-body">
          <div class="form-group"><label>角色编码</label><input v-model="roleForm.roleCode" class="form-input" :disabled="!!editingRole" placeholder="如：FARMER" /></div>
          <div class="form-group"><label>角色名称</label><input v-model="roleForm.roleName" class="form-input" placeholder="如：农户" /></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-ghost" @click="showRoleDialog = false">取消</button>
          <button class="btn btn-primary" @click="handleSaveRole" :disabled="savingRole">{{ savingRole ? '保存中...' : '确定' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api/http'

const roles = ref([])
const selectedRole = ref(null)
const permTree = ref([])
const checkedPerms = ref(new Set())
const permLoading = ref(false)
const saving = ref(false)
const showRoleDialog = ref(false)
const editingRole = ref(null)
const roleForm = reactive({ roleCode: '', roleName: '' })
const savingRole = ref(false)
const builtinRoles = ['ADMIN', 'FARMER', 'MERCHANT']

async function loadRoles() {
  try { roles.value = await adminApi.getRoles() }
  catch { ElMessage.error('加载角色失败') }
}

async function selectRole(r) {
  selectedRole.value = r; await loadPermTree(r.id)
}

async function loadPermTree(roleId) {
  permLoading.value = true
  try {
    permTree.value = await adminApi.getPermissionTree(roleId)
    const checked = new Set()
    for (const mod of permTree.value)
      for (const p of mod.children)
        if (p.checked) checked.add(p.id)
    checkedPerms.value = checked
  } catch { ElMessage.error('加载权限失败') }
  finally { permLoading.value = false }
}

function togglePerm(id) {
  const ns = new Set(checkedPerms.value)
  ns.has(id) ? ns.delete(id) : ns.add(id)
  checkedPerms.value = ns
}

async function handleSavePerms() {
  saving.value = true
  try { await adminApi.assignPermissions(selectedRole.value.id, [...checkedPerms.value]); ElMessage.success('权限已保存') }
  catch (e) { ElMessage.error(e.message || '保存失败') }
  finally { saving.value = false }
}

function editRole(r) { editingRole.value = r; roleForm.roleCode = r.roleCode; roleForm.roleName = r.roleName; showRoleDialog.value = true }

async function handleSaveRole() {
  if (!roleForm.roleCode || !roleForm.roleName) return ElMessage.warning('请填写完整')
  savingRole.value = true
  try {
    if (editingRole.value) { await adminApi.updateRoleInfo(editingRole.value.id, { roleCode: roleForm.roleCode, roleName: roleForm.roleName }); ElMessage.success('已更新') }
    else { await adminApi.createRole({ roleCode: roleForm.roleCode, roleName: roleForm.roleName }); ElMessage.success('已创建') }
    showRoleDialog.value = false; loadRoles()
  } catch (e) { ElMessage.error(e.message || '操作失败') }
  finally { savingRole.value = false }
}

async function handleDeleteRole(r) {
  try {
    await ElMessageBox.confirm(`确定删除「${r.roleName}」？`, '确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await adminApi.deleteRole(r.id); ElMessage.success('已删除')
    if (selectedRole.value?.id === r.id) { selectedRole.value = null; permTree.value = [] }
    loadRoles()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.message) }
}

onMounted(loadRoles)
</script>

<style scoped>
.admin-page { max-width: 1100px; }
.page-heading { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 12px; margin-bottom: 20px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 4px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }

.split-layout { display: flex; gap: 20px; min-height: 400px; }
.role-panel { width: 240px; flex-shrink: 0; }
.perm-panel { flex: 1; min-width: 0; }

.panel-header { font-size: 14px; font-weight: 600; color: #1e1e1e; margin-bottom: 16px; padding-bottom: 8px; border-bottom: 1px solid #e8ebe4; display: flex; align-items: center; gap: 8px; }
.panel-subheader { font-weight: 400; color: #7CCF5F; font-size: 13px; }

.role-list { display: flex; flex-direction: column; gap: 6px; }
.role-card { padding: 14px 16px; border: 1px solid #e8ebe4; border-radius: 12px; cursor: pointer; transition: all 0.15s; background: #fff; }
.role-card:hover { border-color: #d1d5db; }
.role-card.active { border-color: #7CCF5F; box-shadow: 0 0 0 2px rgba(124,207,95,0.1); }
.rc-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.rc-name { font-size: 14px; font-weight: 600; color: #1e1e1e; }
.rc-code { font-size: 11px; color: #9ca3af; font-family: 'JetBrains Mono', monospace; }
.rc-actions { display: flex; gap: 4px; }

.perm-module { background: #f9faf8; border: 1px solid #e8ebe4; border-radius: 12px; padding: 16px; margin-bottom: 12px; }
.perm-module-title { font-size: 13px; font-weight: 600; color: #7CCF5F; margin-bottom: 12px; }
.perm-items { display: flex; flex-wrap: wrap; gap: 8px; }
.perm-item { display: flex; align-items: center; gap: 6px; padding: 6px 12px; background: #fff; border: 1px solid #e8ebe4; border-radius: 8px; font-size: 12px; color: #6b7280; cursor: pointer; transition: all 0.15s; }
.perm-item:hover { border-color: #7CCF5F; }
.perm-item.checked { border-color: #7CCF5F; color: #374151; background: #e8f5e4; }
.perm-item input { accent-color: #7CCF5F; }
.perm-actions { margin-top: 20px; }

.empty-state { text-align: center; padding: 60px; color: #9ca3af; }

.btn { font-family: 'Inter', sans-serif; cursor: pointer; border-radius: 8px; transition: all 0.15s; display: inline-flex; align-items: center; }
.btn-sm { padding: 8px 16px; font-size: 12px; }
.btn-xs { padding: 4px 10px; font-size: 11px; border: none; background: transparent; color: #6b7280; }
.btn-xs:hover { color: #7CCF5F; }
.btn-danger-text:hover { color: #ef4444 !important; }
.btn-primary { background: #7CCF5F; color: #fff; border: none; padding: 8px 20px; font-size: 13px; font-weight: 500; }
.btn-primary:hover { background: #6bc04e; }
.btn-primary:disabled { opacity: 0.5; cursor: default; }
.btn-ghost { padding: 8px 20px; background: transparent; color: #6b7280; border: 1px solid #e8ebe4; font-size: 13px; }
.btn-ghost:hover { background: #f5f7f2; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal-card { background: #fff; border-radius: 20px; padding: 28px; max-width: 90vw; }
.modal-title { font-size: 18px; font-weight: 600; color: #1e1e1e; margin: 0 0 20px; }
.modal-body { display: flex; flex-direction: column; gap: 14px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }
.form-group {}
.form-group label { display: block; font-size: 13px; font-weight: 500; color: #374151; margin-bottom: 6px; }
.form-input { width: 100%; padding: 10px 14px; border: 1px solid #e8ebe4; border-radius: 10px; font-size: 14px; color: #1e1e1e; background: #f9faf8; outline: none; font-family: 'Inter', sans-serif; box-sizing: border-box; }
.form-input:focus { border-color: #7CCF5F; }
</style>
