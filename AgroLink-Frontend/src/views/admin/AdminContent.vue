<template>
  <div class="admin-page">
    <div class="page-heading">
      <h1 class="page-title">内容管理</h1>
      <p class="page-subtitle">平台公告与系统通知</p>
    </div>

    <div class="section-tabs">
      <button :class="['st',{active:section==='notice'}]" @click="section='notice'">平台公告</button>
      <button :class="['st',{active:section==='notify'}]" @click="section='notify'">系统通知</button>
    </div>

    <!-- 公告管理 -->
    <div v-if="section==='notice'" class="table-card">
      <div class="table-toolbar">
        <span class="tb-title">公告列表</span>
        <button class="btn btn-primary btn-sm" @click="showEditor=true;editItem=null;form={title:'',content:''}">+ 发布公告</button>
      </div>
      <div class="table-wrap">
        <table class="dash-table">
          <thead><tr><th>标题</th><th>发布时间</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="n in notices" :key="n.id">
              <td class="title-cell">{{ n.title }}</td>
              <td class="mono time">{{ n.time }}</td>
              <td><span :class="['badge',n.active?'success':'default']">{{ n.active ? '显示中' : '已隐藏' }}</span></td>
              <td><button class="btn btn-xs btn-outline" @click="editNotice(n)">编辑</button></td>
            </tr>
            <tr v-if="notices.length===0"><td colspan="4" class="empty-cell">暂无公告</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 系统通知 -->
    <div v-if="section==='notify'" class="table-card">
      <div class="table-toolbar">
        <span class="tb-title">通知记录</span>
        <button class="btn btn-primary btn-sm" @click="showEditor=true;editItem=null;form={title:'',content:''}">+ 发送通知</button>
      </div>
      <div class="table-wrap">
        <table class="dash-table">
          <thead><tr><th>标题</th><th>接收范围</th><th>发送时间</th><th>状态</th></tr></thead>
          <tbody>
            <tr v-for="n in sysNotifs" :key="n.id">
              <td class="title-cell">{{ n.title }}</td>
              <td>{{ n.scope }}</td>
              <td class="mono time">{{ n.time }}</td>
              <td><span :class="['badge','success']">已发送</span></td>
            </tr>
            <tr v-if="sysNotifs.length===0"><td colspan="4" class="empty-cell">暂无记录</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 编辑器 Modal -->
    <div v-if="showEditor" class="modal-overlay" @click.self="showEditor=false">
      <div class="modal-card">
        <h3 class="modal-title">{{ editItem ? '编辑' : '发布' }}内容</h3>
        <div class="modal-body">
          <div class="form-group"><label>标题</label><input v-model="form.title" class="form-input" /></div>
          <div class="form-group"><label>内容</label><textarea v-model="form.content" class="form-textarea" rows="5"></textarea></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-ghost" @click="showEditor=false">取消</button>
          <button class="btn btn-primary" @click="handleSave">{{ editItem ? '保存' : '发布' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const section = ref('notice')
const showEditor = ref(false)
const editItem = ref(null)
const form = ref({ title: '', content: '' })

const notices = ref([
  { id: 1, title: '平台服务费调整通知', time: '2026-05-10', active: true },
  { id: 2, title: '五一劳动节物流安排', time: '2026-04-28', active: true },
  { id: 3, title: '新用户注册有礼活动', time: '2026-04-20', active: false },
])

const sysNotifs = ref([
  { id: 1, title: '系统维护通知 5/15 2:00-4:00', scope: '全部用户', time: '2026-05-11' },
  { id: 2, title: '新版功能上线公告', scope: '全部用户', time: '2026-05-01' },
])

function editNotice(n) { editItem.value = n; form.value = { title: n.title, content: n.content || '' }; showEditor.value = true }
function handleSave() { ElMessage.success(editItem.value ? '已更新' : '已发布'); showEditor.value = false }
</script>

<style scoped>
.admin-page { max-width: 900px; }
.page-heading { margin-bottom: 16px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 4px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }

.section-tabs { display: flex; gap: 4px; margin-bottom: 20px; background: #f5f7f2; border-radius: 10px; padding: 3px; width: fit-content; }
.st { padding: 8px 18px; border: none; border-radius: 8px; font-size: 13px; font-weight: 500; color: #6b7280; cursor: pointer; font-family: 'Inter', sans-serif; background: transparent; }
.st.active { background: #fff; color: #1e1e1e; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }

.table-card { background: #fff; border: 1px solid #e8ebe4; border-radius: 16px; overflow: hidden; }
.table-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid #f0f0eb; }
.tb-title { font-size: 14px; font-weight: 600; color: #1e1e1e; }
.table-wrap { overflow-x: auto; }
.dash-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.dash-table th { text-align: left; padding: 12px 20px; background: #f9faf8; color: #6b7280; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e8ebe4; }
.dash-table td { padding: 12px 20px; border-bottom: 1px solid #f0f0eb; color: #374151; }
.title-cell { font-weight: 500; color: #1e1e1e; }
.mono { font-family: 'JetBrains Mono', monospace; }
.time { font-size: 12px; color: #6b7280; }

.badge { display: inline-flex; padding: 3px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; }
.badge.success { background: #e8f5e4; color: #7CCF5F; }
.badge.default { background: #f0f0eb; color: #9ca3af; }

.empty-cell { text-align: center; color: #9ca3af; padding: 40px !important; }

.btn { font-family: 'Inter', sans-serif; cursor: pointer; border-radius: 8px; transition: all 0.15s; }
.btn-sm { padding: 8px 16px; font-size: 12px; }
.btn-xs { padding: 4px 10px; font-size: 11px; }
.btn-primary { background: #7CCF5F; color: #fff; border: none; }
.btn-primary:hover { background: #6bc04e; }
.btn-outline { border: 1px solid #e8ebe4; background: transparent; color: #6b7280; }
.btn-outline:hover { border-color: #7CCF5F; color: #7CCF5F; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal-card { background: #fff; border-radius: 20px; width: 520px; max-width: 90vw; padding: 28px; }
.modal-title { font-size: 18px; font-weight: 600; color: #1e1e1e; margin: 0 0 20px; }
.modal-body { display: flex; flex-direction: column; gap: 16px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 13px; font-weight: 500; color: #374151; }
.form-input, .form-textarea { padding: 10px 14px; border: 1px solid #e8ebe4; border-radius: 10px; font-size: 14px; color: #1e1e1e; background: #f9faf8; outline: none; font-family: 'Inter', sans-serif; }
.form-input:focus, .form-textarea:focus { border-color: #7CCF5F; }
.form-textarea { resize: vertical; }
.btn-ghost { padding: 10px 24px; background: transparent; color: #6b7280; border: 1px solid #e8ebe4; font-size: 14px; }
.btn-ghost:hover { background: #f5f7f2; }
</style>
