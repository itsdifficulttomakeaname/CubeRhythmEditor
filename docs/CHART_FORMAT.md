# 铺面格式说明

## JSON 格式规范

铺面文件存放于 `plugins/CubeRhythm/{chartId}.json`，格式如下：

```json
{
  "version": "1.0.0",
  "metadata": {
    "id": "simpletone",
    "title": "simpletone",
    "artist": "CRE",
    "charter": "PiraTom",
    "difficulty": {
      "name": "Tutorial 1",
      "color": "&b",
      "level": 1
    },
    "audio": "cr.simpletone",
    "duration": 51,
    "offset": 0,
    "bpm": 130
  },
  "groupEvents": [...],
  "notes": [...]
}
```

## 音符类型

| 类型          | 颜色       | 触发方式   | 说明              |
|-------------|----------|--------|-----------------|
| tap         | 浅蓝（混凝土）  | 单击     | 基础点击音符          |
| hold        | 白色（混凝土）  | 按住     | 长按音符，无需对准       |
| drag        | 黄色（混凝土）  | 自动（准心） | 准心瞄准自动判定        |
| flick       | 品红（混凝土）  | 自动（视角） | 到达时检测视角方向       |
| double      | 橙色（混凝土）  | 双击     | 两个位置同时击打        |
| execution   | 不可见      | 自动     | 触发预设动作，不计入判定    |
| mine_tap    | 浅蓝（带釉陶瓦） | 不点击    | 地雷音符，点击则MISS    |
| mine_drag   | 黄色（带釉陶瓦） | 不瞄准    | 地雷音符，瞄准则MISS    |
| mine_double | 橙色（带釉陶瓦） | 不点击    | 地雷音符，点击则MISS    |
| fake_tap    | 浅蓝（玻璃）   | —      | 假键，仅表演用，不计分不计连击 |
| fake_hold   | 白色（玻璃）   | —      | 假键，仅表演用，不计分不计连击 |
| fake_drag   | 黄色（玻璃）   | —      | 假键，仅表演用，不计分不计连击 |
| fake_flick  | 品红（玻璃）   | —      | 假键，仅表演用，不计分不计连击 |
| fake_double | 橙色（玻璃）   | —      | 假键，仅表演用，不计分不计连击 |

### 各类型字段

**tap / hold / drag / fake_tap / fake_hold / fake_drag / mine_tap / mine_drag:**
```json
{"type": "tap", "time": 1.5, "face": "w", "position": {"x": 0, "y": 0}, "glowing": false, "tag": ""}
```

**flick / fake_flick:**
```json
{"type": "flick", "time": 3.0, "face": "w", "turn": "left", "glowing": false, "tag": ""}
```
- `turn`: `"left"` 或 `"right"`

**double / fake_double / mine_double:**
```json
{"type": "double", "time": 2.0, "face": "w", "positions": [{"x": -1, "y": 0}, {"x": 1, "y": 0}], "glowing": false, "tag": ""}
```

> **地雷音符说明**：`mine_*` 类型的判定逻辑与普通音符相反——不操作判定为 EXACT（完美），操作则判定为 MISS（错过）。地雷音符计入总音符数和分数，材质为同色带釉陶瓦，落点光标为深红色。自动播放模式下地雷音符会被自动跳过（不交互），正确获得 EXACT。

> **假键说明**：`fake_*` 类型与对应真键字段完全相同，材质为同色玻璃（混凝土→玻璃）。假键从远处生成飞向判定面，过线后静默消失，不计分、不计连击、不可点击，用于纯表演效果。

**execution:**
```json
{
  "type": "execution",
  "time": 0.5,
  "actions": [
    {"type": "title", "enabled": true, "title": "§fCube Rhythm", "subtitle": "", "fadeIn": 10, "stay": 40, "fadeOut": 10},
    {"type": "actionbar", "enabled": true, "text": "§b注意节奏！"},
    {"type": "chat", "enabled": true, "message": "§a提示"},
    {"type": "potion", "enabled": true, "effectType": "SPEED", "duration": 100, "amplifier": 1, "ambient": false, "particles": true, "icon": true},
    {"type": "remove_potion", "enabled": true, "effectType": "SPEED"},
    {"type": "clear_effects", "enabled": true}
  ]
}
```

**execution action 说明：**

| type            | 说明           | 字段                                                                             |
|-----------------|--------------|--------------------------------------------------------------------------------|
| `title`         | 显示标题/副标题     | `title`, `subtitle`, `fadeIn`(tick), `stay`(tick), `fadeOut`(tick)             |
| `actionbar`     | 显示 ActionBar | `text`                                                                         |
| `chat`          | 发送聊天消息       | `message`                                                                      |
| `potion`        | 给予药水效果       | `effectType`, `duration`(tick), `amplifier`, `ambient`, `particles`, `icon`    |
| `remove_potion` | 移除药水效果       | `effectType`                                                                   |
| `clear_effects` | 清除所有药水效果     | —                                                                              |
| `draw_text`     | 在判定面上绘制文字    | `face`, `x`, `y`, `z`, `text`, `scale`, `color`, `duration`(tick)              |
| `draw_line`     | 在判定面上绘制线条    | `face`, `x1`, `y1`, `z1`, `x2`, `y2`, `z2`, `color`, `width`, `duration`(tick) |

> **注意**：旧版的 `blind`、`change_glow_color`、`hide_note`、`easing_motion` 已移除。
> 音符动画功能现由新事件系统（`groupEvents` + `inline events`）替代，提供更强大的关键帧动画能力。
> `hide_note` 功能由 `hide` 事件通道替代。

**draw_text 详细说明：**
```json
{
  "type": "draw_text",
  "enabled": true,
  "face": "w",
  "x": 0, "y": 2, "z": 5,
  "text": "§6READY?",
  "scale": 2.0,
  "color": "#FFFFFF",
  "duration": 40
}
```
- `face`: 判定面（w/a/s/d），决定坐标变换方向
- `x`, `y`: 面内局部坐标（与音符 position 相同坐标系）
- `z`: 距判定面的距离（0=判定面上，正值=远离玩家）
- `text`: 显示文本（支持 `§` 颜色代码）
- `scale`: 文字缩放倍率（默认 1.0）
- `color`: 背景颜色（十六进制，默认透明）
- `duration`: 显示持续时间（tick），到期后自动移除

**draw_line 详细说明：**
```json
{
  "type": "draw_line",
  "enabled": true,
  "face": "w",
  "x1": -2, "y1": 0, "z1": 5,
  "x2": 2, "y2": 0, "z2": 5,
  "color": "#FF5555",
  "width": 0.05,
  "duration": 60
}
```
- `face`: 判定面
- `x1`, `y1`, `z1`: 起点局部坐标
- `x2`, `y2`, `z2`: 终点局部坐标
- `color`: 线条颜色（十六进制，默认白色）
- `width`: 线条宽度（格，默认 0.05）
- `duration`: 显示持续时间（tick），到期后自动移除

### 音符标签

支持单标签和多标签两种写法：

```json
// 单标签（兼容旧格式）
{"tag": "spiral"}

// 多标签
{"tags": ["spiral", "glow_effect"]}
```

标签用于 groupEvent 的 selector 匹配。

## 判定面

| 代码 | 方向 | 旋转角度 | 标记颜色 |
|----|----|------|------|
| w  | 前  | 0°   | 白色   |
| a  | 左  | 90°  | 黄色   |
| s  | 后  | 180° | 橙色   |
| d  | 右  | 270° | 红色   |

坐标范围：x ∈ [-3.5, 3.5]，y ∈ [-3.5, 3.5]，原点在面中心。

---

## 动画事件系统

### 架构流程

```
Chart JSON
  │
  ▼
ChartLoader (解析 groupEvents + inline events，rtime → 绝对时间转换)
  │
  ▼
NoteSpawner (预筛 matchedTracks，时间窗口过滤，落点预计算)
  │
  ▼
TrackEvaluator.evaluate(entity, currentTime) → EvalResult
  │
  ▼
NoteRenderer.updateNote(..., evalResult) → 应用视觉通道
```

### 时间模型

音符的距离公式：
```
distance = speed × 20 × (noteTime + 1.0 - currentTime) + 3
```

其中 `+1.0` 是距离公式的固有偏移。因此音符实际到达判定面（distance=3）的时刻为 `noteTime + 1.0`，记为 **arrivalTime**。

**rtime 转换公式：**
```
absoluteTime = noteTime + 1.0 + rtime
```

- `rtime = 0.0` → 音符到达判定面的时刻
- `rtime = -1.0` → 到达前 1 秒
- `rtime = -0.05` → 到达前 1 tick（实践中用作末帧，避免浮点精度问题）

---

### GroupEvent（群组事件）

群组事件定义在铺面顶层，通过 selector 匹配多个音符，对它们施加相同的动画轨道。

```json
{
  "groupEvents": [
    {
      "selector": {
        "face": "w",
        "type": "tap",
        "tag": "spiral",
        "timeRange": [4.0, 12.0]
      },
      "events": {
        "x": [
          {"time": 4.0, "value": 0, "easing": "linear"},
          {"time": 6.0, "value": 2, "easing": "sineOut"},
          {"time": 8.0, "value": 0, "easing": "sineIn"}
        ]
      }
    }
  ]
}
```

**Selector 字段：**

| 字段          | 类型             | 说明                           |
|-------------|----------------|------------------------------|
| `face`      | string 或 array | 匹配判定面，如 `"w"` 或 `["w", "a"]` |
| `type`      | string 或 array | 匹配音符类型                       |
| `tag`       | string 或 array | 匹配音符标签（交集检查）                 |
| `timeRange` | [start, end]   | 匹配时间范围（闭区间）                  |

- 字段间 AND 组合；同一字段内多值 OR 组合
- 所有字段均可选，省略则不限制

**时间模式：**

groupEvent 的关键帧支持两种时间模式：

1. **绝对时间 `time`** — 所有匹配音符共享同一条时间轴
2. **相对时间 `rtime`** — 每个音符独立享受完整动画周期

```json
// 绝对时间：所有 wave 音符在 t=4.0~8.0 期间共享同一波浪
"x": [
  {"time": 4.0, "value": 0, "easing": "linear"},
  {"time": 6.0, "value": 2, "easing": "sineOut"},
  {"time": 8.0, "value": 0, "easing": "sineIn"}
]

// 相对时间：每个 wave 音符各自从到达前 4 秒开始完整的波浪动画
"x": [
  {"rtime": -4.0, "value": 0, "easing": "linear"},
  {"rtime": -2.0, "value": 2, "easing": "sineOut"},
  {"rtime": -0.05, "value": 0, "easing": "sineIn"}
]
```

使用 `rtime` 时，运行时会为每个匹配音符按其 noteTime 重新计算绝对时间（rebase），确保每个音符都能体验完整的动画周期。

---

### Inline Event（内联事件）

直接写在音符内部，使用 `rtime` 相对时间：

```json
{
  "type": "tap",
  "time": 14.0,
  "face": "w",
  "position": {"x": 0, "y": 1},
  "events": {
    "y": [
      {"rtime": -1.0, "value": 10, "easing": "linear"},
      {"rtime": -0.05, "value": 0, "easing": "quadOut"}
    ]
  }
}
```

上例：音符从 Y 方向偏移 10 格的位置（远超判定面边界）在 1 秒内飞入目标位置 (0, 1)。

---

### 通道枚举

| 通道名      | JSON 字段名             | 类型  | 默认值  | 说明                      |
|----------|----------------------|-----|------|-------------------------|
| X        | `x`                  | 加法  | 0    | 横向偏移（格）                 |
| Y        | `y`                  | 加法  | 0    | 纵向偏移（格）                 |
| Z        | `z`                  | 加法  | 0    | 飞行方向偏移，正值=远离判定面（格）      |
| SCALE_X  | `scale_x` / `scalex` | 乘法  | 1.0  | X 轴缩放倍率                 |
| SCALE_Y  | `scale_y` / `scaley` | 乘法  | 1.0  | Y 轴缩放倍率                 |
| SCALE_Z  | `scale_z` / `scalez` | 乘法  | 1.0  | Z 轴缩放倍率                 |
| —        | `scale`              | 语法糖 | 1.0  | 同时设置 scale_x/y/z        |
| ROTATE   | `rotate`             | 加法  | 0    | 绕飞行方向自转（度）              |
| COLOR_R  | `color_r` / `colorr` | 加法  | -1   | 发光颜色 R（0-255，-1=使用面默认色） |
| COLOR_G  | `color_g` / `colorg` | 加法  | -1   | 发光颜色 G                  |
| COLOR_B  | `color_b` / `colorb` | 加法  | -1   | 发光颜色 B                  |
| COLOR_A  | `color_a` / `colora` | 加法  | 255  | 发光颜色 A                  |
| MATERIAL | `material`           | 离散  | null | 方块材质名（无插值，hold 语义）      |
| HIDE     | `hide`               | 离散  | 0    | 隐藏状态（0=可见，1=隐藏，hold 语义） |

**累积规则（多轨道叠加时）：**
- **加法通道**（X/Y/Z/ROTATE/COLOR_*）：各轨道的值相加
- **乘法通道**（SCALE_X/SCALE_Y/SCALE_Z）：各轨道的值相乘
- **离散通道**（MATERIAL/HIDE）：最后一个有值的轨道生效

---

### 缓动函数

关键帧的 `easing` 字段指定从**上一帧**到**本帧**的插值曲线。首帧的 easing 无意义。

| 缓动名          | 效果         |
|--------------|------------|
| `linear`     | 匀速         |
| `hold`       | 不插值，保持上一帧值 |
| `quadIn`     | 二次方加速      |
| `quadOut`    | 二次方减速      |
| `quadInOut`  | 二次方先加速后减速  |
| `cubicIn`    | 三次方加速      |
| `cubicOut`   | 三次方减速      |
| `cubicInOut` | 三次方先加速后减速  |
| `sineIn`     | 正弦加速       |
| `sineOut`    | 正弦减速       |
| `sineInOut`  | 正弦先加速后减速   |
| `expoIn`     | 指数加速       |
| `expoOut`    | 指数减速       |
| `expoInOut`  | 指数先加速后减速   |
| `backOut`    | 带轻微回弹的减速   |

所有缓动函数输入 t ∈ [0, 1]，输出 [0, 1]（`backOut` 可能略超 1）。

---

### 关键帧采样边界行为

| 条件            | 返回值         |
|---------------|-------------|
| `t <= 首帧时间`   | 首帧的 value   |
| `t >= 末帧时间`   | 通道默认值（非末帧值） |
| 单帧且 `t > 帧时间` | 通道默认值       |

**设计意图**：事件结束后不再影响音符，音符回归默认状态。各通道默认值：
- 加法通道（X/Y/Z/ROTATE）：0（无偏移）
- 乘法通道（SCALE_X/Y/Z）：1.0（无缩放）
- 颜色通道（COLOR_R/G/B）：-1（使用面默认色）
- COLOR_A：255

---

### 时间窗口过滤规则

```
track.getEndTime() <= arrivalTime
```

如果一个事件轨道的最后帧时间超过了音符到达判定面的时刻，该轨道**整体不生效**，音符使用默认线性飞行。

- 按轨道粒度过滤（非按通道）
- groupEvent 和 inline event 均适用
- 使用 `rtime: -0.05` 作为末帧可确保通过过滤（末帧绝对时间 = arrivalTime - 0.05 < arrivalTime）

---

### 落点预计算

音符生成时预计算落点偏移：

```
落点位置 = position + (所有 matchedTracks 中 X 通道最后帧 value 之和,
                       所有 matchedTracks 中 Y 通道最后帧 value 之和)
```

该偏移用于：
- 落点光标（TextDisplay）的渲染位置
- 击中特效的显示位置
- 判定碰撞检测的目标坐标

**注意**：取的是最后帧的 `value` 字段值，而非在 arrivalTime 采样的结果（因为采样 arrivalTime 时已超过末帧，会返回通道默认值 0）。

---

### 旋转轴心修正

BlockDisplay 默认绕角落 (0,0,0) 旋转。为实现绕方块中心旋转：

```java
Vector3f center = new Vector3f(scaleX/2, scaleY/2, scaleZ/2);
Vector3f rotatedCenter = new Vector3f(center);
new Quaternionf().set(rotationAxisAngle).transform(rotatedCenter);
translation.add(center).sub(rotatedCenter);
```

---

### 实践建议

- 末帧使用 `rtime: -0.05` 而非 `rtime: 0.0`，避免浮点精度导致时间窗口过滤失败
- 位置偏移值不受边界限制，可以设置超出 [-3.5, 3.5] 的值实现从画面外飞入的效果
- 周期性动画（正弦波浪等）使用 groupEvent + rtime 模式，确保每个音符都能体验完整周期
- `scale` 语法糖会同时设置 scale_x/y/z 三个通道
- 颜色通道仅在 `glowing: true` 时生效
- `hide` 通道实现闪烁效果：交替设置 value 为 1（隐藏）和 0（显示），使用 `hold` 缓动

**闪烁示例（每 0.2 秒切换一次可见性）：**
```json
"hide": [
  {"rtime": -1.0, "value": 1, "easing": "hold"},
  {"rtime": -0.8, "value": 0, "easing": "hold"},
  {"rtime": -0.6, "value": 1, "easing": "hold"},
  {"rtime": -0.4, "value": 0, "easing": "hold"},
  {"rtime": -0.2, "value": 1, "easing": "hold"},
  {"rtime": -0.05, "value": 0, "easing": "hold"}
]
```

---

## Skript 格式（遗留）

旧版铺面使用两个 `.sk` 文件：
- `{name}_properties.sk`：元数据（registerChart、setBPM）
- `-{name}.sk`：音符数据（tap/hold/drag/flick/double/execution 函数调用）

已提供 Python 转换脚本 `convert_charts.py` 将 Skript 格式批量转换为 JSON。

颜色代码映射：Skript 使用 `&`，JSON 使用 `§`（如 `&b` → `§b`）。

## 加载流程（Java 系统）

1. 插件启动时 `ChartRegistry` 扫描 `plugins/CubeRhythm/*.json`
2. `AsyncChartLoader` 异步加载，回调在主线程执行
3. 加载完成后注册到 `ChartRegistry`，可通过 `/gui` 或 `/play` 访问
