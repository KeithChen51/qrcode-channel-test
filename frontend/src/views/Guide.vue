<template>
  <div class="guide-page">
    <div class="page-header">
      <h1>系统使用指南</h1>
      <p>快速了解地推活码系统的使用方法</p>
    </div>

    <!-- 快速开始 -->
    <el-card class="mb-4">
      <template #header>
        <div class="card-title">
          <LocalIcon name="solar:rocket-bold-duotone" width="24" class="text-primary" />
          <span>快速开始</span>
        </div>
      </template>
      <el-steps :active="0" direction="vertical" finish-status="success">
        <el-step title="配置小程序" description="在【配置管理】页面添加小程序的AppID和AppSecret" />
        <el-step title="创建活动" description="在【活动管理】页面创建推广活动，配置H5跳转页样式" />
        <el-step title="生成二维码" description="在【二维码管理】页面为每个门店/员工生成专属推广码" />
        <el-step title="下载分发" description="下载二维码图片，分发给地推人员使用" />
        <el-step title="查看数据" description="在【扫码看板】查看扫码统计和注册转化数据" />
      </el-steps>
    </el-card>

    <!-- 功能说明 -->
    <el-card class="mb-4">
      <template #header>
        <div class="card-title">
          <LocalIcon name="solar:widget-bold-duotone" width="24" class="text-blue" />
          <span>功能模块说明</span>
        </div>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="配置管理">
          管理微信小程序配置，包括AppID、AppSecret、页面路径等。支持多个小程序配置并可随时切换激活。
        </el-descriptions-item>
        <el-descriptions-item label="活动管理">
          创建营销活动，自定义H5跳转页面的主题色、标题、按钮文字等样式，让每次推广都有独特的落地页。
        </el-descriptions-item>
        <el-descriptions-item label="二维码管理">
          为每个门店和员工生成专属二维码。用户扫码后会先进入H5中转页面，再跳转到小程序，同时记录扫码渠道。
        </el-descriptions-item>
        <el-descriptions-item label="扫码看板">
          实时查看扫码数据统计，包括总扫码次数、注册转化、门店排名等，帮助评估推广效果。
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 二维码生成流程 -->
    <el-card class="mb-4">
      <template #header>
        <div class="card-title">
          <LocalIcon name="solar:qr-code-bold-duotone" width="24" class="text-green" />
          <span>二维码生成流程</span>
        </div>
      </template>
      <div class="flow-diagram">
        <div class="flow-item">
          <div class="flow-icon">
            <LocalIcon name="solar:qr-code-bold-duotone" width="32" />
          </div>
          <div class="flow-text">
            <h4>生成二维码</h4>
            <p>输入门店ID和员工ID</p>
          </div>
        </div>
        <LocalIcon name="solar:arrow-right-bold" class="flow-arrow" />
        <div class="flow-item">
          <div class="flow-icon blue">
            <LocalIcon name="solar:smartphone-bold-duotone" width="32" />
          </div>
          <div class="flow-text">
            <h4>用户扫码</h4>
            <p>进入H5中转页面</p>
          </div>
        </div>
        <LocalIcon name="solar:arrow-right-bold" class="flow-arrow" />
        <div class="flow-item">
          <div class="flow-icon green">
            <LocalIcon name="solar:widget-5-bold-duotone" width="32" />
          </div>
          <div class="flow-text">
            <h4>跳转小程序</h4>
            <p>带渠道参数进入</p>
          </div>
        </div>
        <LocalIcon name="solar:arrow-right-bold" class="flow-arrow" />
        <div class="flow-item">
          <div class="flow-icon purple">
            <LocalIcon name="solar:chart-2-bold-duotone" width="32" />
          </div>
          <div class="flow-text">
            <h4>数据统计</h4>
            <p>记录渠道来源</p>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 常见问题 -->
    <el-card>
      <template #header>
        <div class="card-title">
          <LocalIcon name="solar:question-circle-bold-duotone" width="24" class="text-warning" />
          <span>常见问题</span>
        </div>
      </template>
      <el-collapse accordion>
        <el-collapse-item title="如何获取小程序的AppID和AppSecret？">
          登录微信公众平台 → 开发 → 开发管理 → 开发设置，即可查看AppID和生成AppSecret。
        </el-collapse-item>
        <el-collapse-item title="二维码生成后可以修改门店信息吗？">
          二维码生成后不可修改渠道信息。如需修改，请删除旧二维码并重新生成。
        </el-collapse-item>
        <el-collapse-item title="H5跳转页面可以自定义样式吗？">
          可以。在【活动管理】中创建活动，设置主题色、标题、按钮文字、Logo等样式。在生成二维码时选择对应活动即可。
        </el-collapse-item>
        <el-collapse-item title="扫码数据如何同步到我们的系统？">
          系统提供回调接口，当用户完成注册后，会通过接口回调通知您的后端系统，传递扫码ID、渠道信息等数据。
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import LocalIcon from '@/components/LocalIcon.vue'
</script>

<style scoped lang="scss">
.guide-page {
  max-width: 1000px;
}

.mb-4 {
  margin-bottom: 20px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 600;
  
  .text-primary { color: var(--primary-color); }
  .text-blue { color: #3b82f6; }
  .text-green { color: #10b981; }
  .text-warning { color: #f59e0b; }
}

.flow-diagram {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0;
  flex-wrap: wrap;
  gap: 16px;
}

.flow-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  
  .flow-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, rgba(29, 78, 216, 0.15), rgba(29, 78, 216, 0.05));
    color: var(--primary-color);
    margin-bottom: 12px;
    
    &.blue {
      background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(59, 130, 246, 0.05));
      color: #3b82f6;
    }
    
    &.green {
      background: linear-gradient(135deg, rgba(16, 185, 129, 0.15), rgba(16, 185, 129, 0.05));
      color: #10b981;
    }
    
    &.purple {
      background: linear-gradient(135deg, rgba(139, 92, 246, 0.15), rgba(139, 92, 246, 0.05));
      color: #8b5cf6;
    }
  }
  
  .flow-text {
    h4 {
      margin: 0 0 4px;
      font-size: 14px;
      font-weight: 600;
      color: var(--text-primary);
    }
    
    p {
      margin: 0;
      font-size: 12px;
      color: var(--text-secondary);
    }
  }
}

.flow-arrow {
  font-size: 24px;
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .flow-diagram {
    flex-direction: column;
  }
  
  .flow-arrow {
    transform: rotate(90deg);
  }
}
</style>
