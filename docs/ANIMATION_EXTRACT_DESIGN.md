# 动画模板提取设计方案

## 问题背景

编辑器需要在 `animation_templates/` 目录中编辑动画模板时：
1. JSON 文件必须通过语法校验（编辑器提示无语法错误）
2. 实际应用时只需要提取**有效内容**，而非完整的外层结构

例如，模板文件包含：
```json
{
  "groupEvents": [
    {"selector": {...}, "events": {...}},
    {"selector": {...}, "events": {...}}
  ]
}
```

但实际需要的只是 `[{...}, {...}]` 部分，甚至只是单个 `{...}` 对象。

## 设计方案

### 方案 A：包装器标记法（推荐）

**模板文件格式：**
```json
{
  "_type": "groupEvents",
  "_description": "螺旋运动模板",
  "content": [
    {
      "selector": {
        "tag": "spiral",
        "timeRange": [0, 10]
      },
      "events": {
        "x": [
          {"rtime": -2.0, "value": 0, "easing": "linear"},
          {"rtime": -0.05, "value": 2, "easing": "sineOut"}
        ],
        "y": [
          {"rtime": -2.0, "value": 2, "easing": "linear"},
          {"rtime": -0.05, "value": 0, "easing": "sineInOut"}
        ]
      }
    }
  ]
}
```

**提取规则：**
- `_type` 字段指示提取目标类型（`groupEvents` | `notes` | `events` | `actions`）
- `content` 字段包含实际有效内容
- 提取时忽略 `_` 前缀的元数据字段

**优点：**
- ✅ 完整的 JSON 语法校验
- ✅ 支持注释性元数据（`_description`、`_author`、`_version`）
- ✅ 明确的提取语义
- ✅ 支持单个对象或数组

**提取实现：**
```java
// 伪代码
JsonObject template = parseJson(file);
String type = template.get("_type").getAsString();
JsonElement content = template.get("content");

// content 可能是对象或数组，根据 _type 判断预期结构
if (type.equals("groupEvents")) {
    // content 应为数组
    return content.getAsJsonArray();
} else if (type.equals("events")) {
    // content 应为对象 {"x": [...], "y": [...]}
    return content.getAsJsonObject();
}
```

---

### 方案 B：根数组/对象直接识别

**模板文件格式：**
```json
[
  {
    "selector": {"tag": "wave"},
    "events": {"y": [...]}
  },
  {
    "selector": {"tag": "rotate"},
    "events": {"rotate": [...]}
  }
]
```

或单个对象：
```json
{
  "selector": {"tag": "spiral"},
  "events": {"x": [...], "y": [...]}
}
```

**提取规则：**
- 根元素即有效内容
- 通过结构特征推断类型（包含 `selector` → groupEvent，包含 `type`/`time` → note）

**优点：**
- ✅ 最简洁
- ✅ 直接使用

**缺点：**
- ❌ 无法添加模板元数据（描述、作者等）
- ❌ 类型推断可能不准确

---

### 方案 C：多文件分离

**目录结构：**
```
animation_templates/
  spiral_wave/
    template.json     # 有效内容
    meta.json         # 元数据
```

**template.json：**
```json
[
  {"selector": {...}, "events": {...}}
]
```

**meta.json：**
```json
{
  "type": "groupEvents",
  "description": "螺旋波浪运动",
  "author": "PiraTom"
}
```

**优点：**
- ✅ 内容与元数据完全分离
- ✅ 直接读取即用

**缺点：**
- ❌ 文件管理复杂
- ❌ 需要目录而非单文件

---

## 推荐方案：A（包装器标记法）

### 完整示例

**spiral_motion.json：**
```json
{
  "_type": "groupEvents",
  "_description": "螺旋运动（从左下到中心）",
  "_author": "PiraTom",
  "content": [
    {
      "selector": {
        "tag": "spiral"
      },
      "events": {
        "x": [
          {"rtime": -2.0, "value": -5, "easing": "linear"},
          {"rtime": -0.05, "value": 0, "easing": "sineOut"}
        ],
        "y": [
          {"rtime": -2.0, "value": -5, "easing": "linear"},
          {"rtime": -1.0, "value": 2, "easing": "sineInOut"},
          {"rtime": -0.05, "value": 0, "easing": "sineOut"}
        ],
        "rotate": [
          {"rtime": -2.0, "value": 0, "easing": "linear"},
          {"rtime": -0.05, "value": 720, "easing": "quadOut"}
        ]
      }
    }
  ]
}
```

**inline_events.json：**
```json
{
  "_type": "events",
  "_description": "单音符内联事件（从上方飞入）",
  "content": {
    "y": [
      {"rtime": -1.5, "value": 10, "easing": "linear"},
      {"rtime": -0.05, "value": 0, "easing": "quadOut"}
    ],
    "scale": [
      {"rtime": -1.5, "value": 0.5, "easing": "linear"},
      {"rtime": -0.05, "value": 1.0, "easing": "backOut"}
    ]
  }
}
```

**execution_actions.json：**
```json
{
  "_type": "actions",
  "_description": "标题+音效提示",
  "content": [
    {
      "type": "title",
      "enabled": true,
      "title": "§6GET READY!",
      "subtitle": "",
      "fadeIn": 5,
      "stay": 20,
      "fadeOut": 5
    },
    {
      "type": "potion",
      "enabled": true,
      "effectType": "SPEED",
      "duration": 40,
      "amplifier": 1,
      "ambient": true,
      "particles": false,
      "icon": false
    }
  ]
}
```

### 实现建议

**AnimationTemplateWindow.java 修改：**

1. **复制按钮行为**：提取 `content` 字段内容而非整个 JSON
2. **状态栏提示**：显示模板类型（根据 `_type` 字段）
3. **验证逻辑**：检查必需字段（`_type` 和 `content`）

**提取工具类（新增）：**
```java
public class TemplateExtractor {
    public static String extractContent(String templateJson) {
        JsonObject root = JsonParser.parseString(templateJson).getAsJsonObject();
        
        if (!root.has("content")) {
            // 回退：如果没有包装器，返回原始内容
            return templateJson;
        }
        
        JsonElement content = root.get("content");
        return new GsonBuilder().setPrettyPrinting().create().toJson(content);
    }
    
    public static String getTemplateType(String templateJson) {
        JsonObject root = JsonParser.parseString(templateJson).getAsJsonObject();
        return root.has("_type") ? root.get("_type").getAsString() : "unknown";
    }
}
```

### 使用流程

1. 用户在 `animation_templates/` 中创建 JSON 模板（方案 A 格式）
2. 编辑器加载并显示完整 JSON（通过语法校验）
3. 用户点击"复制"按钮
4. 提取器提取 `content` 字段内容到剪贴板
5. 用户粘贴到主编辑器的 `groupEvents` 数组或音符的 `events` 字段中

---

## `_type` 命名规范

`_type` 的值与铺面 JSON 中的字段/概念直接对应，无需额外映射：

| `_type`       | 对应位置                        | `content` 结构                                  |
|---------------|-----------------------------|-----------------------------------------------|
| `groupEvents` | `chart.groupEvents`         | 数组 `[{selector, events}, ...]`                |
| `groupEvent`  | `chart.groupEvents[i]`      | 单对象 `{selector, events}`                      |
| `inlineEvent` | `note.events`               | 对象 `{x: [...], y: [...]}`                     |
| `track`       | `events.x` / `events.y` 等通道 | 数组 `[{rtime, value, easing}, ...]`            |
| `execution`   | execution 音符整体              | 单对象 `{type:"execution", time, actions:[...]}` |
| `actions`     | `execution.actions`         | 数组 `[{type, ...}, ...]`                       |
| `notes`       | `chart.notes`               | 数组 `[{type, time, ...}, ...]`                 |

---

## 模板包装器功能设计

### 需求

用户手写了一段动画内容（如一组关键帧、一个 groupEvent），但它本身不是合法的完整 JSON（或者是合法的但缺少元数据包装），需要快速将其打包成标准模板文件保存到 `animation_templates/` 目录。

### 入口

在顶部菜单栏（现有"动画模板"按钮旁边）新增按钮 **"模板包装器"**。

### 交互流程

```
用户点击"模板包装器"
       │
       ▼
弹出对话框（TemplateWrapperDialog）
  ┌─────────────────────────────────────────┐
  │ 模板内容（粘贴你的 JSON 片段，不检查格式）  │
  │ ┌─────────────────────────────────────┐ │
  │ │  [多行文本框，可粘贴任意内容]          │ │
  │ └─────────────────────────────────────┘ │
  │                                         │
  │ 模板类型（_type）: [下拉框 ▼]            │
  │   groupEvents / groupEvent / inlineEvent│
  │   execution / actions / track / notes  │
  │                                         │
  │ 模板名称（文件名，不含.json）: [输入框]   │
  │ 描述（_description，可选）:  [输入框]    │
  │ 作者（_author，可选）:       [输入框]    │
  │                                         │
  │          [取消]    [创建模板]            │
  └─────────────────────────────────────────┘
       │
       ▼（点击"创建模板"）
按以下结构写入 animation_templates/{名称}.json：
{
  "_type": "<选择的类型>",
  "_description": "<填写的描述>",
  "_author": "<填写的作者>",
  "content": <用户粘贴的内容（原样写入，不解析）>
}
       │
       ▼
提示"模板已保存：animation_templates/{名称}.json"
（若动画模板窗口已打开则自动 reload）
```

### 关键设计决策

1. **不检查格式**：`content` 字段的值原样写入，不做 JSON 语法验证。这允许用户保存半成品模板或带注释的草稿内容。
   - 实现：直接字符串拼接，不经过 `JsonParser`。
   - 写入结果：`"content": <原始字符串>`——如果原始内容本身不是合法 JSON，文件整体也会变成非法 JSON，但这是用户的选择。

2. **模板名称冲突**：如果同名文件已存在，弹出确认对话框（覆盖 / 取消），不静默覆盖。

3. **_type 下拉选项**：提供预设值，同时允许手动输入自定义类型（可编辑的 JComboBox）。

### 写入格式

```java
// 伪代码（字符串拼接，不用 Gson 序列化整体）
String json = "{\n"
    + "  \"_type\": \"" + type + "\",\n"
    + (desc.isEmpty() ? "" : "  \"_description\": \"" + escapeJson(desc) + "\",\n")
    + (author.isEmpty() ? "" : "  \"_author\": \"" + escapeJson(author) + "\",\n")
    + "  \"content\": " + contentText.strip() + "\n"
    + "}";
Files.writeString(outFile.toPath(), json, StandardCharsets.UTF_8);
```

这样即使 `content` 是合法 JSON，整体文件也格式正确；如果 `content` 不合法，文件不合法，但编辑器仍可显示。

### 涉及的类

| 类                                | 变更                                          |
|----------------------------------|---------------------------------------------|
| `MainWindow.java`                | 菜单栏新增"模板包装器"按钮，点击打开 `TemplateWrapperDialog` |
| `TemplateWrapperDialog.java`（新建） | 包装器对话框，继承 `JDialog`                         |
| `AnimationTemplateWindow.java`   | 无需修改（`reload()` 已存在，直接调用即可）                 |

---

## 问题

**Q: 为什么不用 JSON 注释（`// ...`）？**  
A: 标准 JSON 不支持注释，大多数解析器会报错。`_` 前缀字段是合法的 JSON，同时约定俗成地表示元数据。

**Q: 如果用户忘记使用包装器格式怎么办？**  
A: 提取器支持回退逻辑——如果没有 `content` 字段，返回整个 JSON（方案 B 兼容）。

**Q: 是否需要验证 `content` 结构与 `_type` 是否匹配？**  
A: 不强制。模板包装器也不验证，允许用户灵活保存任意内容。
