# Changelog / 更新日志

> 本文件为中英对照：每个条目先中文、后英文。
> Each entry is given in Chinese first, then in English.

上游历史见 [Clickism/ClickSigns](https://github.com/Clickism/ClickSigns)。以下只记录 ClickSigns-CJK 这个分叉的改动。
For upstream history see [Clickism/ClickSigns](https://github.com/Clickism/ClickSigns). Only the changes made by the ClickSigns-CJK fork are listed below.

## 基线 / Baseline

- 基准提交：`a9fde35`（`2.0-beta-6` 之后的 master HEAD）。上一行是上游该版本遗留的最后一条记录。
- Base commit: `a9fde35` (master HEAD, just after `2.0-beta-6`). The line above is the last entry left by upstream at that point.

---

## 未发布 / Unreleased（2026-10）

### 新增 / Added

- **中文输入**：编辑器文本框接入系统输入法，中文 / 日文 / 韩文可直接组词上屏，候选窗浮于输入框上方。
- **IME input**: the editor text field now drives the OS input method, so Chinese / Japanese / Korean can be composed directly, with the candidate window above the field.
- **中文字形**：随包附带 GNU Unifont 15.0.01（`unihex` 提供器），Minecraft 26.x 缺失的汉字可以正常显示，无需额外资源包。
- **CJK glyphs**: GNU Unifont 15.0.01 ships with the mod (`unihex` provider), so the Han characters missing from Minecraft 26.x render correctly with no extra resource pack.

### 修复 / Fixed

- 文本框缺少插入光标：改为自绘光标，随输入、删除、方向键与鼠标点击实时更新，深浅背景均有对比度。
- Missing text caret: the caret is now self-drawn and updates with typing, deletion, arrow keys and clicks, with contrast on both dark and light backgrounds.
- 装了 Iris 等光影包时路牌整块透明：检测到 Iris / VanillaPipelines 时回落到原版 `entity_translucent_cull` 管线。
- Signs rendering fully transparent with Iris: falls back to the vanilla `entity_translucent_cull` pipeline when Iris / VanillaPipelines is detected.
- 调整 Font Size 时数字变化但渲染尺寸不变：文本控件现在会在每次重建时同步元素。
- Font size changing the number but not the rendered text: the text field now re-syncs its element on every rebuild.
- `Highway Area` / `Highway Area Cancel` 模板显示为缺失贴图：修正了记形符号的资源路径（上游同样存在该问题）。
- `Highway Area` / `Highway Area Cancel` rendering as the missing texture: corrected the pictogram asset path (upstream has the same bug).
- 上游每次重建都重复注册 `onValueChanged`，导致同一段文本被重复提交 N 次。
- Upstream registered `onValueChanged` on every rebuild, re-committing the same text N times.

### 安全 / Security

- 服务端校验路牌编辑：必须在牌子 8 格以内，尺寸受限于合法范围，元素数不超过 256。
- Server-side validation: edits must come from within 8 blocks, dimensions stay in range, at most 256 elements.
- 网络包读取前校验列表长度与枚举 ordinal；读写异常改为抛出而非静默吞掉。
- Packet reads bound-check list sizes and enum ordinals; write/read failures now throw instead of being swallowed.

### 变更 / Changed

- 显示名改为 **ClickSigns-CJK**；mod id 与资源命名空间保持 `clicksigns`，以便作为上游的 drop-in 替换（不影响旧存档与已有资源包）。
- Display name is now **ClickSigns-CJK**; the mod id and namespace stay `clicksigns` so it remains a drop-in replacement for upstream (existing worlds and packs keep working).
- `check_updates` 默认关闭：它按 mod id 查询会命中上游项目，提示玩家安装会覆盖掉中文化改动。
- `check_updates` now defaults to off: the lookup resolves to the upstream project, and installing it would drop the CJK work.
- 随附的 GNU Unifont 补上了许可与署名（`UNIFONT-LICENSE.txt`）。
- The bundled GNU Unifont now ships with its licence and attribution (`UNIFONT-LICENSE.txt`).
