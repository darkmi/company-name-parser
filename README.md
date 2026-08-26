# company-name-parser

[![License Apache 2.0](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-1.8%2B-orange.svg)
![Maven](https://img.shields.io/badge/Maven-3.x-blue.svg)

中文公司名称解析工具（Java 版），从 Python 项目 [companynameparser](https://github.com/shibing624/companynameparser) 移植而来。支持提取公司名称中的**地名**、**品牌名**、**行业词**、**公司后缀**等元素。

## 功能特性

对公司名文本进行解析，识别并提取以下元素：

| 元素 | 说明 | 示例 |
|------|------|------|
| `place` | 地名 | 武汉、泉州、常州,合肥 |
| `brand` | 品牌名（主词） | 海明智业、益念、途畅 |
| `trade` | 行业词 | 电子商务、食品、互联网科技 |
| `suffix` | 公司后缀 | 有限公司、股份有限公司,分公司 |
| `symbol` | 标点符号 | 《》() 等 |

## 环境要求

- Java 1.8+
- Maven 3.x

## 项目依赖

- [jieba-analysis](https://github.com/huaban/jieba-analysis) 1.0.2 — 结巴分词 Java 版
- JUnit 4.11 — 单元测试

## 快速开始

### 构建项目

```bash
git clone https://github.com/your-username/company-name-parser.git
cd company-name-parser
mvn clean package
```

### 作为依赖引入

在 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.darkmi</groupId>
    <artifactId>company-name-parser</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### API 使用

#### 基本用法

```java
import com.darkmi.parser.CompanyNameParser;
import com.darkmi.parser.CompanyName;

CompanyNameParser parser = new CompanyNameParser();

String[] names = {
    "武汉海明智业电子商务有限公司",
    "泉州益念食品有限公司",
    "常州途畅互联网科技有限公司合肥分公司",
    "昆明享亚教育信息咨询有限公司",
};

for (String name : names) {
    CompanyName result = parser.parse(name);
    System.out.println(result);
}
```

**输出：**

```
CompanyName{place='武汉', brand='海明智业', trade='电子商务', suffix='有限公司', symbol=''}
CompanyName{place='泉州', brand='益念', trade='食品', suffix='有限公司', symbol=''}
CompanyName{place='常州,合肥', brand='途畅', trade='互联网科技', suffix='有限公司,分公司', symbol=''}
CompanyName{place='昆明', brand='享亚', trade='教育信息咨询', suffix='有限公司', symbol=''}
```

#### 获取位置信息

通过 `posSensitive` 参数获取各元素在原始字符串中的位置：

```java
CompanyName result = parser.parse("武汉海明智业电子商务有限公司", true, false);

// 获取带位置信息的 Token 列表
result.getPlaceTokens();   // [Token{word='武汉', start=0, end=2}]
result.getBrandTokens();   // [Token{word='海明智业', start=2, end=6}]
result.getTradeTokens();   // [Token{word='电子商务', start=6, end=10}]
result.getSuffixTokens();  // [Token{word='有限公司', start=10, end=14}]
```

#### 启用分词模式

通过 `enableWordSegment` 参数对行业词等进行细粒度分词：

```java
CompanyName result = parser.parse("常州途畅互联网科技有限公司合肥分公司", false, true);

// 行业词会被拆分为更细的词
result.getTrade();   // "互联网,科技"
result.getSuffix();  // "有限公司,分公司"
```

### 方法签名

```java
// 默认解析，返回逗号分隔的字符串结果
CompanyName parse(String name);

// 完整参数
CompanyName parse(String name, boolean posSensitive, boolean enableWordSegment);
```

**参数说明：**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `name` | `String` | — | 待解析的公司名称 |
| `posSensitive` | `boolean` | `false` | `true` 返回带位置信息的 Token 列表，`false` 返回逗号分隔的字符串 |
| `enableWordSegment` | `boolean` | `false` | `true` 对行业词等进行细粒度分词 |

### CompanyName 字段

| 字段 | 类型（默认模式） | 类型（posSensitive 模式） |
|------|------------------|--------------------------|
| `place` | `String` | `List<Token>` |
| `brand` | `String` | `List<Token>` |
| `trade` | `String` | `List<Token>` |
| `suffix` | `String` | `List<Token>` |
| `symbol` | `String` | `List<Token>` |

## 数据字典

项目使用以下词典文件（位于 `src/main/resources/data/`）：

| 文件 | 说明 |
|------|------|
| `china_place.txt` | 中国地名词典 |
| `place_single.txt` | 单字地名辅助词典 |
| `brand.txt` | 品牌名词典 |
| `trade.txt` | 行业词词典 |
| `trade_single.txt` | 单字行业词辅助词典 |
| `suffix.txt` | 公司后缀词典 |
| `suffix_single.txt` | 单字后缀辅助词典 |

## 算法流程

1. **分词** — 使用 jieba-analysis 对公司名进行索引模式分词
2. **词典匹配** — 依次从分词结果中提取地名、后缀、行业词、品牌名
3. **后处理** — 对未匹配的词进行智能归类（根据单字词典判断归属）
4. **合并** — 将相邻的同类词合并（如"常"+"州" → "常州"）

## 命令行使用

构建后可直接运行：

```bash
java -jar target/company-name-parser-1.0-SNAPSHOT.jar
```

## 与 Python 版本的差异

| 特性 | Python 版 | Java 版 |
|------|-----------|---------|
| 分词引擎 | jieba | jieba-analysis (Java) |
| 自定义词典 | 支持 (`set_custom_split_file`) | 待实现 |
| 命令行批量处理 | 支持 | 待实现 |
| 评估脚本 | 支持 | 待实现 |

## 项目结构

```
company-name-parser/
├── pom.xml                          # Maven 构建配置
├── src/
│   ├── main/
│   │   ├── java/com/darkmi/parser/
│   │   │   ├── CompanyNameParser.java  # 核心解析器
│   │   │   ├── CompanyName.java        # 解析结果实体
│   │   │   └── Token.java              # 带位置信息的词单元
│   │   └── resources/data/
│   │       ├── china_place.txt         # 地名词典
│   │       ├── brand.txt               # 品牌词典
│   │       ├── trade.txt               # 行业词典
│   │       ├── suffix.txt              # 后缀词典
│   │       └── ...                     # 辅助词典
│   └── test/
│       └── java/com/darkmi/
│           └── AppTest.java
├── reference/                           # Python 原始项目参考
└── LICENSE
```

## Todo

- [ ] 支持自定义分词词典
- [ ] 支持命令行批量处理
- [ ] 补充单元测试
- [ ] 发布到 Maven Central

## License

**Apache License 2.0** — 与原 Python 项目保持一致。

## 致谢

- 原 Python 项目：[companynameparser](https://github.com/shibing624/companynameparser) by [shibing624](https://github.com/shibing624)
- 分词引擎：[jieba-analysis](https://github.com/huaban/jieba-analysis)
