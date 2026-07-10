# 动画模板系统

动画模板是一段预定义的 `events` 片段，可以直接复制到音符的 `events` 字段或 groupEvent 的 `events` 字段中使用。

---

## 设计思路

模板按**效果类型**分类，每个模板提供：
- 可直接使用的 JSON 片段
- 关键参数说明（哪些数值需要根据实际情况调整）
- 适用场景

**参数约定：**
- `DURATION`：动画持续时间（秒），通常 0.5~2.0
- `AMPLITUDE`：偏移幅度（格），正值=右/上/远，负值=左/下/近
- 末帧统一用 `rtime: -0.05` 避免时间窗口过滤失败

---

## 一、入场动画（Entrance）

音符从某个方向飞入落点，适合 inline event。

### 1.1 从上方落下

```json
"events": {
  "y": [
    {"rtime": -1.0, "value": 10, "easing": "linear"},
    {"rtime": -0.05, "value": 0, "easing": "quadOut"}
  ]
}
```
- `value: 10`：起始高度偏移，越大越高
- 缓动改为 `cubicOut` 效果更夸张，`sineOut` 更柔和

### 1.2 从下方升起

```json
"events": {
  "y": [
    {"rtime": -1.0, "value": -10, "easing": "linear"},
    {"rtime": -0.05, "value": 0, "easing": "quadOut"}
  ]
}
```

### 1.3 从左侧滑入

```json
"events": {
  "x": [
    {"rtime": -1.0, "value": -10, "easing": "linear"},
    {"rtime": -0.05, "value": 0, "easing": "sineOut"}
  ]
}
```
- `value: -10`：从左侧（负方向）飞入；改为正值则从右侧飞入

### 1.4 从极远处冲入（Z轴）

```json
"events": {
  "z": [
    {"rtime": -1.0, "value": 40, "easing": "linear"},
    {"rtime": -0.05, "value": 0, "easing": "expoOut"}
  ]
}
```
- `value: 40`：起始距离偏移，越大越远；`expoOut` 产生急刹车感

### 1.5 弧线飞入（X+Y组合）

```json
"events": {
  "x": [
    {"rtime": -1.0, "value": -10, "easing": "linear"},
    {"rtime": -0.05, "value": 0, "easing": "sineInOut"}
  ],
  "y": [
    {"rtime": -1.0, "value": -3, "easing": "linear"},
    {"rtime": -0.5, "value": 3, "easing": "quadOut"},
    {"rtime": -0.05, "value": 3, "easing": "quadIn"}
  ]
}
```
- X 轴匀速滑入，Y 轴先上扬后保持，形成抛物线轨迹
- 调整 Y 的中间帧 value 和时间可改变弧线形状

### 1.6 回弹落入（backOut）

```json
"events": {
  "y": [
    {"rtime": -1.0, "value": 10, "easing": "linear"},
    {"rtime": -0.05, "value": 0, "easing": "backOut"}
  ]
}
```
- `backOut` 会轻微过冲再弹回，有弹性感

### 1.7 分段脉冲飞入

```json
"events": {
  "z": [
    {"rtime": -1.5, "value": 40, "easing": "linear"},
    {"rtime": -1.125, "value": 30, "easing": "sineInOut"},
    {"rtime": -0.75, "value": 20, "easing": "sineInOut"},
    {"rtime": -0.375, "value": 10, "easing": "sineInOut"},
    {"rtime": -0.05, "value": 0, "easing": "sineInOut"}
  ]
}
```
- 每段等距推进，产生"一步一步走近"的节奏感
- 可配合 BPM 调整每段时间间隔

---

## 二、闪烁动画（Blink）

使用 `hide` 通道，适合 groupEvent + tag 批量应用。

### 2.1 快速闪烁（5次，每0.2秒）

```json
"events": {
  "hide": [
    {"rtime": -1.0, "value": 1, "easing": "hold"},
    {"rtime": -0.8, "value": 0, "easing": "hold"},
    {"rtime": -0.6, "value": 1, "easing": "hold"},
    {"rtime": -0.4, "value": 0, "easing": "hold"},
    {"rtime": -0.2, "value": 1, "easing": "hold"},
    {"rtime": -0.05, "value": 0, "easing": "hold"}
  ]
}
```

### 2.2 慢速闪烁（3次，每0.5秒）

```json
"events": {
  "hide": [
    {"rtime": -1.5, "value": 1, "easing": "hold"},
    {"rtime": -1.0, "value": 0, "easing": "hold"},
    {"rtime": -0.5, "value": 1, "easing": "hold"},
    {"rtime": -0.05, "value": 0, "easing": "hold"}
  ]
}
```

### 2.3 渐显闪烁（闪烁+缩放，fade_blink 风格）

```json
"events": {
  "hide": [
    {"rtime": -1.5, "value": 1, "easing": "hold"},
    {"rtime": -1.2, "value": 0, "easing": "hold"},
    {"rtime": -0.9, "value": 1, "easing": "hold"},
    {"rtime": -0.6, "value": 0, "easing": "hold"},
    {"rtime": -0.05, "value": 0, "easing": "hold"}
  ],
  "scale": [
    {"rtime": -1.5, "value": 1.5, "easing": "linear"},
    {"rtime": -0.05, "value": 1.0, "easing": "quadOut"}
  ]
}
```
- 闪烁同时从大缩小，产生"出现感"

### 2.4 加速闪烁（越来越快）

```json
"events": {
  "hide": [
    {"rtime": -2.0, "value": 1, "easing": "hold"},
    {"rtime": -1.5, "value": 0, "easing": "hold"},
    {"rtime": -1.0, "value": 1, "easing": "hold"},
    {"rtime": -0.6, "value": 0, "easing": "hold"},
    {"rtime": -0.3, "value": 1, "easing": "hold"},
    {"rtime": -0.15, "value": 0, "easing": "hold"},
    {"rtime": -0.05, "value": 0, "easing": "hold"}
  ]
}
```

---

## 三、缩放动画（Scale）

### 3.1 弹出（从小变大）

```json
"events": {
  "scale": [
    {"rtime": -0.5, "value": 0.0, "easing": "linear"},
    {"rtime": -0.05, "value": 1.0, "easing": "backOut"}
  ]
}
```

### 3.2 脉冲（放大再缩回）

```json
"events": {
  "scale": [
    {"rtime": -0.5, "value": 1.0, "easing": "linear"},
    {"rtime": -0.25, "value": 2.0, "easing": "quadOut"},
    {"rtime": -0.05, "value": 1.0, "easing": "quadIn"}
  ]
}
```

### 3.3 压扁飞入（X轴压缩+Y轴拉伸）

```json
"events": {
  "scale_x": [
    {"rtime": -0.5, "value": 0.2, "easing": "linear"},
    {"rtime": -0.05, "value": 1.0, "easing": "cubicOut"}
  ],
  "scale_y": [
    {"rtime": -0.5, "value": 3.0, "easing": "linear"},
    {"rtime": -0.05, "value": 1.0, "easing": "cubicOut"}
  ]
}
```

---

## 四、旋转动画（Rotate）

`rotate` 通道绕飞行方向（Z轴）自转，单位为度。

### 4.1 顺时针旋转飞入

```json
"events": {
  "rotate": [
    {"rtime": -1.0, "value": 180, "easing": "linear"},
    {"rtime": -0.05, "value": 0, "easing": "cubicOut"}
  ]
}
```

### 4.2 持续旋转（到达前一直转）

```json
"events": {
  "rotate": [
    {"rtime": -2.0, "value": 720, "easing": "linear"},
    {"rtime": -0.05, "value": 0, "easing": "linear"}
  ]
}
```
- 2秒内转2圈（720°），匀速

### 4.3 旋转+缩放组合（陀螺飞入）

```json
"events": {
  "rotate": [
    {"rtime": -1.0, "value": 360, "easing": "linear"},
    {"rtime": -0.05, "value": 0, "easing": "sineOut"}
  ],
  "scale": [
    {"rtime": -1.0, "value": 0.1, "easing": "linear"},
    {"rtime": -0.05, "value": 1.0, "easing": "cubicOut"}
  ]
}
```

---

## 五、颜色动画（Color，需 glowing: true）

### 5.1 颜色渐变（蓝→红）

```json
"events": {
  "color_r": [
    {"rtime": -1.0, "value": 0, "easing": "linear"},
    {"rtime": -0.05, "value": 255, "easing": "linear"}
  ],
  "color_g": [
    {"rtime": -1.0, "value": 0, "easing": "linear"},
    {"rtime": -0.05, "value": 0, "easing": "linear"}
  ],
  "color_b": [
    {"rtime": -1.0, "value": 255, "easing": "linear"},
    {"rtime": -0.05, "value": 0, "easing": "linear"}
  ]
}
```

### 5.2 彩虹闪烁（RGB循环）

```json
"events": {
  "color_r": [
    {"rtime": -1.5, "value": 255, "easing": "hold"},
    {"rtime": -1.0, "value": 0, "easing": "hold"},
    {"rtime": -0.5, "value": 0, "easing": "hold"},
    {"rtime": -0.05, "value": 255, "easing": "hold"}
  ],
  "color_g": [
    {"rtime": -1.5, "value": 0, "easing": "hold"},
    {"rtime": -1.0, "value": 255, "easing": "hold"},
    {"rtime": -0.5, "value": 0, "easing": "hold"},
    {"rtime": -0.05, "value": 0, "easing": "hold"}
  ],
  "color_b": [
    {"rtime": -1.5, "value": 0, "easing": "hold"},
    {"rtime": -1.0, "value": 0, "easing": "hold"},
    {"rtime": -0.5, "value": 255, "easing": "hold"},
    {"rtime": -0.05, "value": 0, "easing": "hold"}
  ]
}
```

---

## 六、波浪动画（Wave，适合 groupEvent）

批量应用于多个音符，每个音符独立体验完整周期（使用 rtime）。

### 6.1 X轴正弦波浪

```json
"events": {
  "x": [
    {"rtime": -4.0, "value": 0, "easing": "linear"},
    {"rtime": -3.5, "value": 2, "easing": "sineOut"},
    {"rtime": -3.0, "value": 0, "easing": "sineInOut"},
    {"rtime": -2.5, "value": -2, "easing": "sineInOut"},
    {"rtime": -2.0, "value": 0, "easing": "sineIn"},
    {"rtime": -1.5, "value": 2, "easing": "sineOut"},
    {"rtime": -1.0, "value": 0, "easing": "sineInOut"},
    {"rtime": -0.5, "value": -2, "easing": "sineInOut"},
    {"rtime": -0.05, "value": 0, "easing": "sineIn"}
  ]
}
```
- 幅度 ±2 格，周期 1 秒，共 4 个周期
- 调整 `value` 改变幅度，调整时间间隔改变频率

### 6.2 Y轴上下波浪

```json
"events": {
  "y": [
    {"rtime": -4.0, "value": 0, "easing": "linear"},
    {"rtime": -3.5, "value": 2, "easing": "sineOut"},
    {"rtime": -3.0, "value": 0, "easing": "sineInOut"},
    {"rtime": -2.5, "value": -2, "easing": "sineInOut"},
    {"rtime": -2.0, "value": 0, "easing": "sineIn"},
    {"rtime": -1.5, "value": 2, "easing": "sineOut"},
    {"rtime": -1.0, "value": 0, "easing": "sineInOut"},
    {"rtime": -0.5, "value": -2, "easing": "sineInOut"},
    {"rtime": -0.05, "value": 0, "easing": "sineIn"}
  ]
}
```

### 6.3 螺旋飞入（X+Y波浪+Z远处）

```json
"events": {
  "x": [
    {"rtime": -2.0, "value": 0, "easing": "linear"},
    {"rtime": -1.5, "value": 3, "easing": "sineOut"},
    {"rtime": -1.0, "value": 0, "easing": "sineInOut"},
    {"rtime": -0.5, "value": -3, "easing": "sineInOut"},
    {"rtime": -0.05, "value": 0, "easing": "sineIn"}
  ],
  "y": [
    {"rtime": -2.0, "value": 3, "easing": "linear"},
    {"rtime": -1.5, "value": 0, "easing": "sineInOut"},
    {"rtime": -1.0, "value": -3, "easing": "sineInOut"},
    {"rtime": -0.5, "value": 0, "easing": "sineInOut"},
    {"rtime": -0.05, "value": 0, "easing": "sineIn"}
  ],
  "z": [
    {"rtime": -2.0, "value": 20, "easing": "linear"},
    {"rtime": -0.05, "value": 0, "easing": "cubicOut"}
  ]
}
```
- X 和 Y 相位差 90°，形成圆形螺旋轨迹

---

## 七、组合模板（复合效果）

### 7.1 强调出现（弹出+闪烁）

```json
"events": {
  "scale": [
    {"rtime": -0.8, "value": 0.0, "easing": "linear"},
    {"rtime": -0.3, "value": 1.3, "easing": "backOut"},
    {"rtime": -0.05, "value": 1.0, "easing": "quadIn"}
  ],
  "alpha": [
    {"rtime": -0.8, "value": 0.0, "easing": "linear"},
    {"rtime": -0.05, "value": 1.0, "easing": "sineOut"}
  ]
}
```

### 7.2 危险警告（闪烁+颜色，需 glowing: true）

```json
"events": {
  "hide": [
    {"rtime": -2.0, "value": 1, "easing": "hold"},
    {"rtime": -1.6, "value": 0, "easing": "hold"},
    {"rtime": -1.2, "value": 1, "easing": "hold"},
    {"rtime": -0.8, "value": 0, "easing": "hold"},
    {"rtime": -0.4, "value": 1, "easing": "hold"},
    {"rtime": -0.05, "value": 0, "easing": "hold"}
  ],
  "color_r": [
    {"rtime": -2.0, "value": 255, "easing": "linear"},
    {"rtime": -0.05, "value": 255, "easing": "linear"}
  ],
  "color_g": [
    {"rtime": -2.0, "value": 0, "easing": "linear"},
    {"rtime": -0.05, "value": 0, "easing": "linear"}
  ],
  "color_b": [
    {"rtime": -2.0, "value": 0, "easing": "linear"},
    {"rtime": -0.05, "value": 0, "easing": "linear"}
  ]
}
```

---

## 八、groupEvent 使用示例

将上述模板应用于带特定 tag 的所有音符：

```json
"groupEvents": [
  {
    "selector": {
      "tag": "wave",
      "face": "w"
    },
    "events": {
      "x": [
        {"rtime": -4.0, "value": 0, "easing": "linear"},
        {"rtime": -3.5, "value": 2, "easing": "sineOut"},
        {"rtime": -3.0, "value": 0, "easing": "sineInOut"},
        {"rtime": -2.5, "value": -2, "easing": "sineInOut"},
        {"rtime": -2.0, "value": 0, "easing": "sineIn"},
        {"rtime": -1.5, "value": 2, "easing": "sineOut"},
        {"rtime": -1.0, "value": 0, "easing": "sineInOut"},
        {"rtime": -0.5, "value": -2, "easing": "sineInOut"},
        {"rtime": -0.05, "value": 0, "easing": "sineIn"}
      ]
    }
  },
  {
    "selector": {
      "tag": "blink"
    },
    "events": {
      "hide": [
        {"rtime": -1.0, "value": 1, "easing": "hold"},
        {"rtime": -0.8, "value": 0, "easing": "hold"},
        {"rtime": -0.6, "value": 1, "easing": "hold"},
        {"rtime": -0.4, "value": 0, "easing": "hold"},
        {"rtime": -0.2, "value": 1, "easing": "hold"},
        {"rtime": -0.05, "value": 0, "easing": "hold"}
      ]
    }
  }
]
```

对应音符只需加 tag：
```json
{"type": "tap", "time": 5.0, "face": "w", "position": {"x": 0, "y": 0}, "glowing": false, "tag": "wave"}
{"type": "tap", "time": 6.0, "face": "w", "position": {"x": 1, "y": 0}, "glowing": false, "tag": "blink"}
```

---

## 九、快速参考

| 效果    | 核心通道          | 推荐缓动      | 适用场景         |
|-------|---------------|-----------|--------------|
| 从上落下  | y: 10→0       | quadOut   | 普通入场         |
| 从侧面滑入 | x: ±10→0      | sineOut   | 横向入场         |
| 从远处冲入 | z: 40→0       | expoOut   | 强调音符         |
| 弧线飞入  | x+y组合         | sineInOut | 流畅轨迹         |
| 快速闪烁  | hide 交替       | hold      | 警告/节奏        |
| 弹出    | scale: 0→1    | backOut   | 强调出现         |
| 旋转飞入  | rotate: 360→0 | cubicOut  | 特殊音符         |
| 波浪摆动  | x/y 正弦        | sineInOut | groupEvent批量 |
| 螺旋    | x+y相位差90°     | sineInOut | 视觉特效         |

---

## 编辑器集成方案

### 现状分析

CubeRhythmEditor（Swing 桌面应用）目前的 events 编辑方式：
- `Note` 对象有 `events` 字段（`JsonObject`），但没有专门的 UI 编辑界面
- 所有 events 编辑通过右侧"铺面 JSON 编辑器"（RSyntaxTextArea）手写完成
- `groupEvents` 在导出时始终输出为空数组，目前无 UI 支持

模板的核心价值：减少手写 JSON 的负担，让谱师通过 GUI 快速获取模板内容并粘贴使用。

---

### 方案：动画模板副 GUI

#### 入口

在铺面 JSON 编辑框区域添加一个**"动画模板"按钮**，点击后打开独立副 GUI 窗口。

---

#### 副 GUI 规格

**窗口尺寸**：1024 × 768

**布局**：左右分栏
- 默认比例：左侧 1/4，右侧 3/4
- 分割线可由用户自由拖动调整比例
- 左侧：模板列表
- 右侧：模板预览编辑区

**左侧——模板列表**
- 显示所有已加载模板的名称（文件名或模板内定义的 `name` 字段）
- 点击列表项，将对应模板内容加载到右侧预览区
- 列表按文件名字母顺序排列

**右侧——模板预览编辑区**
- 使用与主窗口铺面编辑器相同的 `JsonEditorPanel`（RSyntaxTextArea，Monokai 主题，JSON 语法高亮）
- 模板内容加载到内存后显示在此处，用户可自由修改
- 修改仅影响当前预览内容，**不写回模板文件**

**右下角——两个独立按钮**
- **"复制"按钮**：将右侧预览区的当前内容写入系统剪贴板，窗口保持打开，用户可继续操作
- **"关闭"按钮**：直接关闭副 GUI 窗口

**右侧状态/提示区**
- 位于预览编辑区下方、按钮行上方，显示一行状态文字：
  - 目录为空时：`animation_templates/ 目录中暂无模板，请添加 .json 文件`
  - 文件读取异常时：显示具体错误信息（如文件损坏、编码异常等）
  - 复制成功后：`已复制到剪贴板`

**窗口模态**：**非模态独立窗口**（`JFrame`），可与主窗口并排操作，不阻塞主窗口交互。

**入口按钮位置**：追加到主窗口铺面 JSON 编辑器区域现有按钮行（与"重新加载"、"排序"同行）。

**美观设计**：需在实现时根据整体 UI 风格进行适配，包括但不限于字体、间距、边框、颜色主题的统一。

---

#### 模板文件存储

**目录**：程序所在目录下的 `animation_templates/` 文件夹

**初始化**：程序首次运行时，若 `animation_templates/` 目录不存在，自动创建该目录（空目录，不预置任何文件）

**加载时机**：每次打开动画模板副 GUI 时，主动扫描并读取 `animation_templates/` 目录下所有 `.json` 文件，加载到内存中展示

**模板文件格式**：标准 JSON 文件，文件名即为模板在列表中的显示名称（去掉 `.json` 后缀）。文件内容为模板的 `events` 片段，例如：

```json
{
  "y": [
    {"rtime": -1.0, "value": 10, "easing": "linear"},
    {"rtime": -0.05, "value": 0, "easing": "quadOut"}
  ]
}
```

用户将本文档中的模板片段保存为 `.json` 文件放入该目录即可使用。

---

### 已确认决策

1. **合并策略**：同一通道已有关键帧时，**覆盖**。避免加法叠加产生意外偏移。

2. **HOLD/FLICK 的 scale_z**：不为 `scale_z` 设计动画模板。所有模板均不包含 `scale_z` 通道。

3. **颜色通道范围检测**：在模板参数输入框中对颜色通道（`color_r/g/b/a`）添加范围检测（0-255），超出范围时在输入框旁提示异常，阻止应用直到用户修正。
