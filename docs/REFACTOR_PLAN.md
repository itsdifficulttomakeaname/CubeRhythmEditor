# CubeRhythmEditor 谱面格式升级改造计划

## 一、改造目标

将编辑器从旧版谱面格式升级到新版格式（参考 `CHART_FORMAT.md`），主要包括：

1. **新增音符类型**：mine_tap, mine_drag, mine_double（地雷音符）
2. **支持动画事件系统**：
   - GroupEvent（群组事件，顶层配置）
   - Inline Event（内联事件，音符内部的 events 字段）
3. **多标签支持**：从单标签 `tag` 升级到多标签 `tags` 数组（兼容旧格式）
4. **完整的 metadata 结构**：difficulty 从字符串升级为对象
5. **专用 JSON 编辑器**：带语法高亮、错误检查的文本编辑组件

---

## 二、需要修改的文件清单

### 2.1 核心数据模型

| 文件              | 修改内容                                                  | 优先级 |
|-----------------|-------------------------------------------------------|-----|
| `NoteType.java` | 添加 MINE_TAP, MINE_DRAG, MINE_DOUBLE 枚举值               | 高   |
| `Note.java`     | 添加 `events` 字段（JsonObject），添加 `tags` 字段（List<String>） | 高   |

### 2.2 UI 组件

| 文件                                 | 修改内容                         | 优先级 |
|------------------------------------|------------------------------|-----|
| `MainWindow.java`                  | 更新 JSON 导入导出逻辑，集成新的 JSON 编辑器 | 高   |
| `ui/NoteTypePanel.java`            | 添加地雷音符类型的选择按钮                | 中   |
| **新建** `ui/JsonEditorPanel.java`   | 专用 JSON 编辑器组件（语法高亮）          | 高   |
| **新建** `ui/EventEditorDialog.java` | Inline Event 编辑对话框（可选）       | 低   |

### 2.3 工具类

| 文件                          | 修改内容                 | 优先级 |
|-----------------------------|----------------------|-----|
| **新建** `ChartMetadata.java` | 谱面元数据模型类             | 中   |
| **新建** `GroupEvent.java`    | GroupEvent 数据模型类（可选） | 低   |

---

## 三、详细改造步骤

### 阶段 1：数据模型升级（必须）

#### 1.1 NoteType.java
```java
// 修改 flick 颜色为品红色（统一）
FLICK_LEFT("Flick←", "flick", new Color(255, 0, 153)),
FLICK_RIGHT("Flick→", "flick", new Color(255, 0, 153)),

// 修改 fake_flick 颜色为品红色
FAKE_FLICK("FakeFlick", "fake_flick", new Color(255, 0, 153)),

// 添加三个新枚举值（地雷音符）
MINE_TAP("MineTap", "mine_tap", new Color(0, 120, 215)),
MINE_DRAG("MineDrag", "mine_drag", new Color(255, 215, 0)),
MINE_DOUBLE("MineDouble", "mine_double", new Color(255, 140, 0))
```

#### 1.2 Note.java
```java
// 添加新字段
private JsonObject events;  // inline events
private List<String> tags = new ArrayList<>();  // 多标签支持

// 添加辅助方法
public boolean isMine() {
    return type == NoteType.MINE_TAP || type == NoteType.MINE_DRAG || 
           type == NoteType.MINE_DOUBLE;
}

public boolean isDouble() {
    return type == NoteType.DOUBLE || type == NoteType.FAKE_DOUBLE || 
           type == NoteType.MINE_DOUBLE;
}

// 标签兼容性方法
public void setTag(String tag) {
    this.tags.clear();
    if (tag != null && !tag.isEmpty()) {
        this.tags.add(tag);
    }
}

public String getTag() {
    return tags.isEmpty() ? "" : tags.get(0);
}

// 更新 getColor()：flick 统一品红色
public Color getColor() {
    return switch (type) {
        case TAP, FAKE_TAP, MINE_TAP -> new Color(0, 120, 215);
        case DRAG, FAKE_DRAG, MINE_DRAG -> new Color(255, 215, 0);
        case DOUBLE, FAKE_DOUBLE, MINE_DOUBLE -> new Color(255, 140, 0);
        case EXECUTION -> new Color(128, 128, 128);
        case FLICK_LEFT, FLICK_RIGHT, FAKE_FLICK -> new Color(255, 0, 153);
        case HOLD, FAKE_HOLD -> new Color(0, 200, 0);
    };
}
```

#### 1.3 MainWindow 渲染逻辑更新
```java
// 新增：绘制地雷音符的对角线交叉图案
private void drawCrossPattern(Graphics g, int x, int y, int w, int h, int alpha) {
    Color crossColor = new Color(139, 0, 0, alpha); // 深红色
    Graphics2D g2 = (Graphics2D) g;
    g2.setColor(crossColor);
    g2.setStroke(new BasicStroke(2f));
    // 左上到右下
    g2.drawLine(x, y, x + w, y + h);
    // 右上到左下
    g2.drawLine(x + w, y, x, y + h);
}

// 在 paintComponent 中的渲染逻辑：
// 绘制音符后，根据类型叠加效果
if (note.isFake()) {
    drawCheckerboard(g, rx, ry, rs, rs, alpha);  // 已有
}
if (note.isMine()) {
    drawCrossPattern(g, rx, ry, rs, rs, alpha);  // 新增
}
```

---

### 阶段 2：JSON 导入导出升级（必须）

#### 2.1 MainWindow.noteToJson() 方法改造

**当前问题**：
- 只输出 notes 数组，缺少完整的谱面结构（metadata, groupEvents）
- 不支持 inline events
- 不支持多标签

**改造方案**：
```java
// 新增方法：导出完整谱面 JSON
private String exportFullChartJson() {
    JsonObject root = new JsonObject();
    root.addProperty("version", "1.0.0");
    
    // metadata
    JsonObject metadata = new JsonObject();
    metadata.addProperty("id", songNameField.getText().trim());
    metadata.addProperty("title", songNameField.getText().trim());
    metadata.addProperty("artist", composerField.getText().trim());
    metadata.addProperty("charter", chartAuthorField.getText().trim());
    
    // difficulty 对象
    JsonObject difficulty = new JsonObject();
    difficulty.addProperty("name", difficultyField.getText().trim());
    difficulty.addProperty("level", 1);  // 需要添加 UI 输入
    difficulty.addProperty("color", "&b");  // 需要添加 UI 输入
    metadata.add("difficulty", difficulty);
    
    metadata.addProperty("audio", "cr." + songNameField.getText().trim());
    metadata.addProperty("duration", parseDuration());
    metadata.addProperty("offset", parseOffset());
    metadata.addProperty("bpm", parseBpm());
    
    root.add("metadata", metadata);
    
    // groupEvents（暂时为空数组，后续可扩展）
    root.add("groupEvents", new JsonArray());
    
    // notes
    JsonArray notesArray = new JsonArray();
    for (Note note : noteManager.getNotes()) {
        notesArray.add(noteToJsonObject(note));
    }
    root.add("notes", notesArray);
    
    return new GsonBuilder().setPrettyPrinting().create().toJson(root);
}

// 改造：单个音符转 JsonObject
private JsonObject noteToJsonObject(Note note) {
    JsonObject json = new JsonObject();
    json.addProperty("type", note.getType().getJsonName());
    json.addProperty("time", note.getTimeMicroseconds() / 1_000_000.0);
    
    // 根据音符类型添加字段
    if (note.isExecution()) {
        json.add("actions", note.getActions() != null ? note.getActions() : new JsonArray());
    } else if (note.isFlick()) {
        json.addProperty("face", note.getDirection());
        json.addProperty("turn", note.getFlickDirection());
        json.addProperty("glowing", note.isGlowing());
        addTagsToJson(json, note);
    } else if (note.isDouble() || note.isMine() && note.getType() == NoteType.MINE_DOUBLE) {
        json.addProperty("face", note.getDirection());
        JsonArray positions = new JsonArray();
        JsonObject pos1 = new JsonObject();
        pos1.addProperty("x", note.getX());
        pos1.addProperty("y", note.getY());
        JsonObject pos2 = new JsonObject();
        pos2.addProperty("x", note.getX2());
        pos2.addProperty("y", note.getY2());
        positions.add(pos1);
        positions.add(pos2);
        json.add("positions", positions);
        json.addProperty("glowing", note.isGlowing());
        addTagsToJson(json, note);
    } else {
        // tap, drag, hold, mine_tap, mine_drag
        json.addProperty("face", note.getDirection());
        JsonObject position = new JsonObject();
        position.addProperty("x", note.getX());
        position.addProperty("y", note.getY());
        json.add("position", position);
        json.addProperty("glowing", note.isGlowing());
        addTagsToJson(json, note);
    }
    
    // inline events
    if (note.getEvents() != null) {
        json.add("events", note.getEvents());
    }
    
    return json;
}

// 标签输出（兼容单标签和多标签）
private void addTagsToJson(JsonObject json, Note note) {
    if (note.getTags().isEmpty()) {
        json.addProperty("tag", "");
    } else if (note.getTags().size() == 1) {
        json.addProperty("tag", note.getTags().get(0));
    } else {
        JsonArray tagsArray = new JsonArray();
        for (String tag : note.getTags()) {
            tagsArray.add(tag);
        }
        json.add("tags", tagsArray);
    }
}
```

#### 2.2 parseNoteFromJsonObject() 方法改造

**需要添加**：
- mine_* 类型的解析
- inline events 的解析
- 多标签的解析

```java
// 在 switch 中添加 mine 类型
case "mine_tap":
case "mine_drag": {
    String face = noteObj.get("face").getAsString();
    boolean glowing = noteObj.get("glowing").getAsBoolean();
    JsonObject position = noteObj.getAsJsonObject("position");
    double x = position.get("x").getAsDouble();
    double y = position.get("y").getAsDouble();
    NoteType noteType = "mine_tap".equals(type) ? NoteType.MINE_TAP : NoteType.MINE_DRAG;
    Note note = new Note(x, y, noteType, timeMicroseconds, face, glowing);
    parseTagsFromJson(noteObj, note);
    parseEventsFromJson(noteObj, note);
    return note;
}

case "mine_double": {
    String face = noteObj.get("face").getAsString();
    boolean glowing = noteObj.get("glowing").getAsBoolean();
    JsonArray positions = noteObj.getAsJsonArray("positions");
    JsonObject pos1 = positions.get(0).getAsJsonObject();
    JsonObject pos2 = positions.get(1).getAsJsonObject();
    double x1 = pos1.get("x").getAsDouble();
    double y1 = pos1.get("y").getAsDouble();
    double x2 = pos2.get("x").getAsDouble();
    double y2 = pos2.get("y").getAsDouble();
    Note note = new Note(x1, y1, x2, y2, NoteType.MINE_DOUBLE, timeMicroseconds, face, glowing);
    parseTagsFromJson(noteObj, note);
    parseEventsFromJson(noteObj, note);
    return note;
}

// 辅助方法：解析标签（兼容单标签和多标签）
private void parseTagsFromJson(JsonObject noteObj, Note note) {
    if (noteObj.has("tags")) {
        JsonArray tagsArray = noteObj.getAsJsonArray("tags");
        for (int i = 0; i < tagsArray.size(); i++) {
            note.getTags().add(tagsArray.get(i).getAsString());
        }
    } else if (noteObj.has("tag")) {
        String tag = noteObj.get("tag").getAsString();
        if (!tag.isEmpty()) {
            note.getTags().add(tag);
        }
    }
}

// 辅助方法：解析 inline events
private void parseEventsFromJson(JsonObject noteObj, Note note) {
    if (noteObj.has("events")) {
        note.setEvents(noteObj.getAsJsonObject("events"));
    }
}
```

---

### 阶段 3：专用 JSON 编辑器（推荐）

#### 3.1 创建 JsonEditorPanel.java

**功能需求**：
1. 语法高亮（关键字、字符串、数字、布尔值）
2. 行号显示
3. 自动缩进
4. 括号匹配
5. JSON 格式验证（实时或保存时）
6. 错误提示

**技术方案选择**：

**方案 A：使用 RSyntaxTextArea（推荐）**
- 优点：功能完善，开箱即用，支持多种语言高亮
- 缺点：需要添加外部依赖

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.fifesoft</groupId>
    <artifactId>rsyntaxtextarea</artifactId>
    <version>3.3.4</version>
</dependency>
```

```java
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;

public class JsonEditorPanel extends JPanel {
    private RSyntaxTextArea textArea;
    private RTextScrollPane scrollPane;
    
    public JsonEditorPanel() {
        setLayout(new BorderLayout());
        
        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        
        // 设置主题
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            theme.apply(textArea);
        } catch (IOException e) {
            // 使用默认主题
        }
        
        scrollPane = new RTextScrollPane(textArea);
        scrollPane.setLineNumbersEnabled(true);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void setText(String text) {
        textArea.setText(text);
    }
    
    public String getText() {
        return textArea.getText();
    }
    
    public boolean validateJson() {
        try {
            new Gson().fromJson(textArea.getText(), JsonElement.class);
            return true;
        } catch (JsonSyntaxException e) {
            JOptionPane.showMessageDialog(this, 
                "JSON 格式错误：" + e.getMessage(), 
                "语法错误", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
```

**方案 B：自定义简单高亮（轻量级）**
- 优点：无外部依赖，轻量
- 缺点：功能有限，需要自己实现

```java
public class SimpleJsonEditorPanel extends JPanel {
    private JTextPane textPane;
    private StyledDocument doc;
    
    // 定义样式
    private Style keywordStyle;
    private Style stringStyle;
    private Style numberStyle;
    
    public SimpleJsonEditorPanel() {
        setLayout(new BorderLayout());
        
        textPane = new JTextPane();
        doc = textPane.getStyledDocument();
        
        // 初始化样式
        keywordStyle = textPane.addStyle("Keyword", null);
        StyleConstants.setForeground(keywordStyle, new Color(204, 120, 50));
        StyleConstants.setBold(keywordStyle, true);
        
        stringStyle = textPane.addStyle("String", null);
        StyleConstants.setForeground(stringStyle, new Color(106, 135, 89));
        
        numberStyle = textPane.addStyle("Number", null);
        StyleConstants.setForeground(numberStyle, new Color(104, 151, 187));
        
        // 添加文档监听器实现实时高亮
        doc.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { highlightSyntax(); }
            public void removeUpdate(DocumentEvent e) { highlightSyntax(); }
            public void changedUpdate(DocumentEvent e) { highlightSyntax(); }
        });
        
        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void highlightSyntax() {
        // 简单的正则匹配高亮
        // 实现细节...
    }
}
```

#### 3.2 集成到 MainWindow

```java
// 替换原有的 chartLogTextArea
private JsonEditorPanel jsonEditorPanel;

// 在 initUI() 中
jsonEditorPanel = new JsonEditorPanel();
// 添加到布局中...

// 更新方法
private void updateChartLogJson() {
    String json = exportFullChartJson();
    jsonEditorPanel.setText(json);
}

// 重载方法
private void reloadNotesFromJson() throws Exception {
    if (!jsonEditorPanel.validateJson()) {
        return;
    }
    String jsonText = jsonEditorPanel.getText();
    // 解析逻辑...
}
```

---

### 阶段 4：UI 增强（可选）

#### 4.1 添加地雷音符到 NoteTypePanel

```java
// 在 NoteTypePanel 中添加三个新按钮
// 使用不同的图标或颜色标识地雷音符
```

#### 4.2 添加 metadata 编辑面板

```java
// 新增字段
private JTextField difficultyLevelField;  // 难度等级
private JComboBox<String> difficultyColorCombo;  // 难度颜色
```

#### 4.3 GroupEvent 编辑器（高级功能）

**选项 1**：仅在 JSON 文本中手动编辑（推荐）
- 优点：实现简单，灵活性高
- 缺点：需要用户熟悉 JSON 格式

**选项 2**：创建专门的 UI 编辑器
- 优点：用户友好
- 缺点：开发工作量大，UI 复杂

---

## 四、技术决策（已确定）

### 决策 5：音符外观与玩法设计 ✅

#### 音符颜色方案（编辑器内）

| 类型          | 编辑器颜色              | 材质效果    | 备注                         |
|-------------|--------------------|---------|----------------------------|
| tap         | 浅蓝 `(0,120,215)`   | 实心填充    | 不变                         |
| hold        | 绿色 `(0,200,0)`     | 实心填充    | **保留绿色**（与文档白色不同，为了编辑器区分度） |
| drag        | 黄色 `(255,215,0)`   | 实心填充    | 不变                         |
| flick       | 品红 `(255,0,153)`   | 实心填充    | LEFT 和 RIGHT 统一颜色          |
| double      | 橙色 `(255,140,0)`   | 实心填充    | 不变                         |
| execution   | 灰色 `(128,128,128)` | 实心填充    | 不变                         |
| mine_tap    | 浅蓝 `(0,120,215)`   | 对角线交叉图案 | 地雷标识                       |
| mine_drag   | 黄色 `(255,215,0)`   | 对角线交叉图案 | 地雷标识                       |
| mine_double | 橙色 `(255,140,0)`   | 对角线交叉图案 | 地雷标识                       |
| fake_tap    | 浅蓝 `(0,120,215)`   | 棋盘格图案   | 已有                         |
| fake_hold   | 绿色 `(0,200,0)`     | 棋盘格图案   | **保留绿色**                   |
| fake_drag   | 黄色 `(255,215,0)`   | 棋盘格图案   | 已有                         |
| fake_flick  | 品红 `(255,0,153)`   | 棋盘格图案   | 修正颜色                       |
| fake_double | 橙色 `(255,140,0)`   | 棋盘格图案   | 已有                         |

#### 材质效果渲染规则

```
普通音符（混凝土）：实心填充
假键（玻璃）：    棋盘格图案覆盖（已有 drawCheckerboard 方法）
地雷（带釉陶瓦）：对角线交叉图案覆盖（新增 drawCrossPattern 方法）
```

**地雷音符对角线交叉图案示例**：
```
┌─────────┐
│╲       ╱│
│  ╲   ╱  │
│    ╳    │
│  ╱   ╲  │
│╱       ╲│
└─────────┘
```

#### Flick 枚举处理

**决策**：保持 FLICK_LEFT 和 FLICK_RIGHT 两个枚举值不变

**导出逻辑**：
```java
// 导出时统一为 "flick" 类型，通过枚举名推断 turn
json.addProperty("type", "flick");  // 统一类型名
String turn = (note.getType() == NoteType.FLICK_LEFT) ? "left" : "right";
json.addProperty("turn", turn);
```

**导入逻辑**：
```java
// 导入时根据 turn 字段选择枚举
case "flick": {
    String turn = noteObj.get("turn").getAsString();
    NoteType flickType = "left".equals(turn) ? NoteType.FLICK_LEFT : NoteType.FLICK_RIGHT;
    // ...
}
```

#### 坐标范围

**决策**：保留 -3.5 ~ 3.5（编辑器内部使用，导出时不做限制）

---

### 决策 1：JSON 编辑器方案 ✅
**选择**：使用 RSyntaxTextArea

**理由**：
- 功能完善：语法高亮、行号、括号匹配、代码折叠
- 稳定可靠：成熟的开源库
- 开箱即用：无需自己实现复杂的高亮逻辑

**实施**：
```xml
<!-- 添加到 pom.xml -->
<dependency>
    <groupId>com.fifesoft</groupId>
    <artifactId>rsyntaxtextarea</artifactId>
    <version>3.3.4</version>
</dependency>
```

---

### 决策 2：高级功能编辑方式 ✅
**选择**：所有高级功能均在 JSON 文本中手动编辑

**包括**：
- GroupEvent（群组事件）
- Inline Event（音符内联事件）
- Execution Actions（执行动作）

**理由**：
- 这些结构复杂且灵活，可视化编辑器开发成本高
- JSON 编辑更直观，便于复制粘贴和批量修改
- 用户可以参考 CHART_FORMAT.md 文档编写
- 避免 UI 过度复杂化

---

### 决策 3：多标签 UI ✅
**选择**：支持逗号分隔的多标签输入

**UI 表现**：
```
标签: [spiral, glow_effect, wave_group]
```

**解析逻辑**：
```java
// 输入: "spiral, glow_effect, wave_group"
// 输出: ["spiral", "glow_effect", "wave_group"]

String[] tagArray = tagField.getText().split(",");
for (String tag : tagArray) {
    String trimmed = tag.trim();
    if (!trimmed.isEmpty()) {
        note.getTags().add(trimmed);
    }
}
```

**用例**：
- 单标签：输入 `spiral`
- 多标签：输入 `spiral, wave, glow`
- 空标签：留空

**理由**：
- 简单实用，符合用户习惯
- 不增加 UI 复杂度
- 实现成本低

---

### 决策 4：Metadata 编辑完善程度 ✅
**选择**：添加所有字段的 UI 输入，分阶段实现

**第一阶段（立即实现）**：
- ✅ id（使用 songName）
- ✅ title
- ✅ artist
- ✅ charter
- ✅ bpm
- ✅ duration
- ✅ offset

**第二阶段（后续完善）**：
- ⏳ difficulty.name（难度名称）
- ⏳ difficulty.level（难度等级 1-15）
- ⏳ difficulty.color（难度颜色，下拉选择）
- ⏳ audio（音频资源 ID）

**UI 设计**：
```
┌─────────────────────────────────┐
│ 谱面信息 ▼                       │
├─────────────────────────────────┤
│ 谱面 ID:    [simpletone      ]  │
│ 歌曲名:     [Simple Tone     ]  │
│ 作曲:       [CRE             ]  │
│ 谱师:       [PiraTom         ]  │
│ BPM:        [130             ]  │
│ 时长(秒):   [51              ]  │
│ 偏移(毫秒): [0               ]  │
│                                 │
│ 难度名称:   [Tutorial 1      ]  │
│ 难度等级:   [1          ] ▼    │
│ 难度颜色:   [AQUA       ] ▼    │
└─────────────────────────────────┘
```

**理由**：
- 完整的 metadata 是新格式的核心
- 分阶段实现降低风险
- 第一阶段复用现有字段，第二阶段添加新字段

---

## 五、实施计划（按决策调整）

### 第一阶段：核心功能升级（必须实现）

#### 1.1 数据模型升级
- [ ] NoteType.java：添加 MINE_TAP, MINE_DRAG, MINE_DOUBLE
- [ ] NoteType.java：修正 FLICK_LEFT/FLICK_RIGHT/FAKE_FLICK 颜色为品红 `(255,0,153)`
- [ ] Note.java：添加 `events` 字段（JsonObject）
- [ ] Note.java：添加 `tags` 字段（List<String>）
- [ ] Note.java：添加 `isMine()` 辅助方法
- [ ] Note.java：更新 `isDouble()` 包含 MINE_DOUBLE
- [ ] Note.java：更新 `getColor()` 统一 flick 品红色

#### 1.2 JSON 编辑器集成
- [ ] 添加 RSyntaxTextArea 依赖到 pom.xml
- [ ] 创建 JsonEditorPanel.java
- [ ] 在 MainWindow 中替换 chartLogTextArea

#### 1.3 JSON 导入导出升级
- [ ] 实现 `exportFullChartJson()` 方法（完整格式）
- [ ] 改造 `noteToJsonObject()` 方法（支持 mine 类型、多标签、events）
- [ ] 改造 `parseNoteFromJsonObject()` 方法（解析新字段）
- [ ] 添加 `parseTagsFromJson()` 辅助方法（兼容单/多标签）
- [ ] 添加 `parseEventsFromJson()` 辅助方法
- [ ] Flick 导出统一为 `"type": "flick"`，通过枚举推断 turn

#### 1.4 多标签 UI 支持
- [ ] 修改标签输入框的提示文本："标签 (逗号分隔)"
- [ ] 实现逗号分隔解析逻辑
- [ ] 实现标签数组转逗号分隔字符串（用于回显）

#### 1.5 音符渲染更新
- [ ] 新增 `drawCrossPattern()` 方法（地雷音符对角线交叉图案，深红色）
- [ ] 在 paintComponent 中为地雷音符叠加交叉图案
- [ ] 确保 fake 和 mine 的渲染效果互不冲突

---

### 第二阶段：UI 完善（建议实现）

#### 2.1 地雷音符 UI
- [ ] 在 NoteTypePanel 添加三个地雷音符按钮
- [ ] 设置地雷音符的颜色和图标
- [ ] 更新 hasCoordinates() 等辅助方法支持 mine 类型

#### 2.2 Metadata 编辑完善
- [ ] 添加 difficulty.name 输入框
- [ ] 添加 difficulty.level 输入框（1-15）
- [ ] 添加 difficulty.color 下拉选择框（预设颜色代码）
- [ ] 更新 exportFullChartJson() 使用新字段

---

### 第三阶段：高级功能（可选）

#### 3.1 GroupEvent 支持
- [ ] 在 JSON 编辑器中手动编辑（无需额外开发）
- [ ] 提供 GroupEvent 模板示例

#### 3.2 Inline Event 支持
- [ ] 在 JSON 编辑器中手动编辑（无需额外开发）
- [ ] 提供 Inline Event 模板示例

#### 3.3 文档和示例
- [ ] 创建 EXAMPLES.md（常用配置示例）
- [ ] 创建 TEMPLATES.md（GroupEvent 和 Event 模板）

---

## 六、兼容性考虑

### 向后兼容
- 保留对旧格式的读取支持（单标签 `tag`）
- 导出时优先使用新格式，但保持字段兼容

### 数据迁移
- 旧谱面导入时自动转换为新格式
- 提供"导出为旧格式"选项（可选）

---

## 七、测试计划

### 单元测试
- Note 对象的序列化/反序列化
- 标签兼容性测试（单标签 ↔ 多标签）
- 地雷音符的正确解析

### 集成测试
- 完整谱面的导入导出
- JSON 编辑器的语法验证
- UI 操作的正确性

### 回归测试
- 确保旧功能不受影响
- 旧谱面文件的兼容性

---

## 八、预估工作量

| 阶段               | 工作量          | 说明                     |
|------------------|--------------|------------------------|
| 阶段 1：数据模型升级      | 1-2 小时       | 简单的枚举和字段添加             |
| 阶段 2：JSON 导入导出升级 | 3-4 小时       | 逻辑较复杂，需要仔细测试           |
| 阶段 3：JSON 编辑器集成  | 2-3 小时       | 使用 RSyntaxTextArea 较简单 |
| 阶段 4：UI 增强       | 2-4 小时       | 取决于实现的功能数量             |
| 测试和调试            | 2-3 小时       | 确保稳定性                  |
| **总计**           | **10-16 小时** | 分阶段实施可降低风险             |

---

## 九、风险和注意事项

### 风险
1. **JSON 解析错误**：复杂的嵌套结构可能导致解析失败
2. **UI 性能**：大型谱面的 JSON 高亮可能影响性能
3. **数据丢失**：导入导出过程中可能丢失数据

### 缓解措施
1. 充分的错误处理和日志记录
2. 对大文件禁用实时高亮，改为手动触发
3. 导出前自动备份，提供撤销功能

---

## 十、后续扩展方向

1. **可视化 Timeline**：时间轴上显示所有音符和事件
2. **动画预览**：实时预览 groupEvent 和 inline event 的效果
3. **模板系统**：常用的 event 配置保存为模板
4. **批量编辑**：选中多个音符批量修改属性
5. **导入导出插件**：支持其他谱面格式的转换

---

## 十一、决策总结

| 问题                   | 决策                   | 理由                            |
|----------------------|----------------------|-------------------------------|
| JSON 编辑器             | RSyntaxTextArea      | 功能完善，稳定可靠                     |
| GroupEvent 编辑        | JSON 手动编辑            | 结构复杂，JSON 更灵活                 |
| Inline Event 编辑      | JSON 手动编辑            | 结构复杂，JSON 更灵活                 |
| Execution Actions 编辑 | JSON 手动编辑            | 类型多样，JSON 更灵活                 |
| 多标签 UI               | 逗号分隔输入               | 简单实用，符合习惯                     |
| Metadata 完善度         | 分阶段实现所有字段            | 降低风险，逐步完善                     |
| Flick 枚举             | 保持 LEFT/RIGHT 两个枚举值  | 导出时统一为 "flick"，通过枚举名推断 turn   |
| 坐标范围                 | 保留 -3.5 ~ 3.5        | 编辑器内部使用，不做限制                  |
| 地雷音符外观               | 对角线交叉图案（深红色）         | 与假键棋盘格区分，视觉上明确"危险"            |
| Hold 颜色              | 保留绿色 `(0,200,0)`     | 编辑器中需要区分度，白色在浅灰背景上不明显         |
| Flick 颜色             | 统一为品红色 `(255,0,153)` | 与文档一致，LEFT/RIGHT 通过 turn 字段区分 |

---

**文档版本**：v3.0（已整合音符外观与玩法决策）  
**创建日期**：2026-05-29  
**更新日期**：2026-05-30  
**作者**：Claude (Kiro)  
**状态**：✅ 所有决策已确定，可开始实施
