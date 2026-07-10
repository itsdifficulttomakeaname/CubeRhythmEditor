# 渲染性能优化设计

## 当前瓶颈分析

### 1. 每帧重复创建 Color 对象（最高频热点）

每个音符每帧都执行：
```java
g.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), alpha));
```
alpha 值是连续变化的（0-255），无法直接缓存，但可以改用 `AlphaComposite` + 固定 Color，避免每帧 new Color。

**方案：** 改为 `g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha/255f))`，fillColor 直接用 `note.getColor()` 不带 alpha。结束后恢复 composite。

### 2. 每帧对音符列表排序

```java
notesToShow.sort((a, b) -> Double.compare(...));  // 每帧执行
```

`getNotesForCurrentBeat` 返回的列表每帧都重新排序，而 NoteManager 内部已经用 TreeMap（时间有序）存储。

**方案：** `getNotesForCurrentBeat` 直接返回已排序的结果，或在 NoteManager 层保证返回顺序，删掉 paintComponent 里的 sort 调用。

### 3. 每帧强转 Graphics2D

```java
Graphics2D g2 = (Graphics2D) g;  // 在音符循环内部
```

**方案：** 在 paintComponent 开头转换一次，整个方法复用同一个 g2。

### 4. 无 RenderingHints，抗锯齿未启用

当前没有设置任何 RenderingHints，渲染质量和 Swing 默认行为一致，但缺少加速提示。

**方案：** 在 paintComponent 开头设置：
```java
g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
```
注意：若优先画质可改为 QUALITY，但编辑器场景优先流畅度。

### 5. 静态背景每帧重绘

网格线、刻度线、坐标轴等静态内容在每次 repaint 时都重新绘制，而它们只在窗口大小变化或切换网格模式时才需要更新。

**方案：** 用一个 `BufferedImage gridCache` 缓存静态背景，仅在 panel 尺寸变化或 `showGrid` 切换时重新生成，每帧直接 `g.drawImage(gridCache, 0, 0, null)` 即可。

---

## 优化优先级

| 优先级 | 优化项                            | 预期收益               | 改动风险       |
|-----|--------------------------------|--------------------|------------|
| 高   | 消除循环内 new Color                | 减少 GC 压力，帧率提升明显    | 低          |
| 高   | 删掉 paintComponent 内 sort       | 减少每帧 O(n log n) 排序 | 低          |
| 中   | Graphics2D 提前转换                | 消除循环内强转开销          | 低          |
| 中   | 添加 RENDER_SPEED RenderingHints | 提示 JVM 优化渲染路径      | 低          |
| 低   | 静态背景 BufferedImage 缓存          | 减少背景重绘开销           | 中（需管理缓存失效） |

---

## 开放问题

1. `getNotesForCurrentBeat` 当前是否已保证返回有序结果？若是，直接删掉 sort 即可，否则需改 NoteManager。 **用户回答** 我不知道，你查一下代码吧
2. 是否需要抗锯齿（`VALUE_ANTIALIAS_ON`）？开启后音符边缘更平滑但有性能代价，编辑器场景建议关闭。 **用户回答** 关闭
3. 两个 paintComponent（网格/无网格）代码几乎重复，是否同步优化，还是只优化一个先验证效果？ **用户回答** 直接一口气搞定 

**用户选择** 可以整合几个方案使得优化最大化，以优化为主，保证基本视觉效果即可
