# AnkiWords

> **AnkiDroid 背单词定制版** —— 基于开源 [AnkiDroid](https://github.com/ankidroid/Anki-Android) 的间隔重复记忆应用，针对「背单词」场景做了体验优化。

## 这是什么

AnkiWords 是 [AnkiDroid](https://github.com/ankidroid/Anki-Android) 的一个定制分支（fork），保留原版全部能力，同时修复/优化了几个背单词时的高频痛点。

## 本仓库相对原版的改动

### 1. 显示答案后也能自动播放发音

**痛点**：很多单词卡的发音只挂在正面，点「显示答案」后就不再自动发音，背单词时很别扭。

**改动**：答案面自动播放时，如果答案面没有音频、而正面有音频（典型的单词卡发音场景），自动回退到正面音频播放。新旧两套复习界面均生效。

**涉及文件**：

- `AnkiDroid/src/main/java/com/ichi2/anki/cardviewer/CardMediaPlayer.kt`
- `AnkiDroid/src/main/java/com/ichi2/anki/AbstractFlashcardViewer.kt`

### 2. 底部答案按钮默认放大 2 倍

**痛点**：复习界面底部「显示答案 / 重来 / 困难 / 良好 / 简单」按钮偏小，容易误触。

**改动**：按钮高度默认值由 100% 调整为 200%，新旧两套复习界面均已修改。

**涉及文件**：

- `AnkiDroid/src/main/java/com/ichi2/anki/settings/Prefs.kt`

> 注意：默认值只对「全新安装 / 清除应用数据」生效；已在使用的安装包可在「设置 → 无障碍 → Button size」手动调整。

### 3. 国内镜像加速依赖下载（构建优化）

Gradle 仓库加入国内镜像（阿里云 / 腾讯），方便国内网络拉取依赖，加速构建。

### 4. 回答进度条

**痛点**：复习时看不到整批任务的完成进度，心里没数。

**改动**：新学习界面顶部增加学习进度条与百分比，随作答实时推进（本次任务量完成后自动归零重来）。

**涉及文件**：

- `AnkiDroid/src/main/java/com/ichi2/anki/ui/windows/reviewer/ReviewerViewModel.kt`
- `AnkiDroid/src/main/java/com/ichi2/anki/ui/windows/reviewer/ReviewerFragment.kt`
- `AnkiDroid/src/main/res/layout/fragment_reviewer.xml`

### 5. 自动同步默认开启

**改动**：安装后默认开启 AnkiWeb 自动同步，不用手动去设置里翻开关（可在「设置 → 同步」关闭）。

**涉及文件**：

- `AnkiDroid/src/main/java/com/ichi2/anki/settings/Prefs.kt`
- `AnkiDroid/src/main/res/xml/preferences_sync.xml`

### 6. 「重来」音频三重复播开关

**痛点**：背单词时点了「重来」（不认识），希望多听几遍发音加深印象，但原版点了就立刻跳下一张。

**改动**：新学习界面右上角新增「循环」图标开关（暗=关 / 亮=开，状态持久保存）。开启后点「重来」时，当前面的音频会连播 **3 遍**（答案面没有音频时自动回退播放正面的），播完才切下一张；关闭时与原版行为一致，点了立刻切卡。播放途中点任何其他按钮会立即取消播放并执行新操作，不会把评分记错卡。

**涉及文件**：

- `AnkiDroid/src/main/java/com/ichi2/anki/cardviewer/CardMediaPlayer.kt`
- `AnkiDroid/src/main/java/com/ichi2/anki/ui/windows/reviewer/ReviewerViewModel.kt`
- `AnkiDroid/src/main/java/com/ichi2/anki/ui/windows/reviewer/ReviewerFragment.kt`
- `AnkiDroid/src/main/res/layout/fragment_reviewer.xml`
- `AnkiDroid/src/main/java/com/ichi2/anki/settings/Prefs.kt`

### 7. 复习记忆率统计

**痛点**：一批到期单词复习完，想知道这次记住的比例，原版没有直接的口径。

**改动**：

- 进入学习界面时锁定「到期复习数」作为分母（避免复习过程中计数递减的坑）；**新卡不参与统计**。
- 到期复习卡**首次作答**计分：「良好」（及困难/简单）= 记住，「重来」= 忘记；答错掉进学习队列后再答对不重复计分。
- 学习界面顶部进度条旁实时显示「记 xx%」。
- 全部学完后弹窗显示本次记忆率（记住 n / 共 m）。

**涉及文件**：

- `AnkiDroid/src/main/java/com/ichi2/anki/ui/windows/reviewer/ReviewerViewModel.kt`
- `AnkiDroid/src/main/java/com/ichi2/anki/ui/windows/reviewer/ReviewerFragment.kt`
- `AnkiDroid/src/main/java/com/ichi2/anki/ui/windows/reviewer/StudyCounts.kt`
- `AnkiDroid/src/main/res/layout/fragment_reviewer.xml`

## 核心功能（继承自 AnkiDroid）

- **自定义单词卡**：自建笔记类型、字段与卡片模板，自由添加单词、释义、音标、发音、例句、图片等内容
- **间隔重复**：AI 优化的 [FSRS 算法](https://github.com/open-spaced-repetition)，科学安排复习节奏
- **自动播放音频**：发音次数可设置，配合本项目的「显示答案后自动发音」体验更顺
- **发音**：文本转语音（TTS）+ 内置音频
- 夜间模式、白板、学习统计、AnkiWeb 云同步
- 支持文本 / 图片 / 音频 / MathJax 公式
- 支持从词典等其他应用分享导入生词

## 构建

构建方式与原版 AnkiDroid 一致，详见[原仓库文档](https://github.com/ankidroid/Anki-Android)。

## 许可证 & 致谢

本项目是 [AnkiDroid](https://github.com/ankidroid/Anki-Android) 的衍生作品，遵循 [GPL-3.0](COPYING) 许可证。

感谢 AnkiDroid 与 Anki 团队的开源贡献。
