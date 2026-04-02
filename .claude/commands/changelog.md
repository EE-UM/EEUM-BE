다음 절차에 따라 CHANGELOG.md를 생성하거나 업데이트하세요.

## 1. 범위 파악

먼저 아래 명령을 실행해 현재 상태를 파악하세요:

```bash
# 최신 태그 확인
git tag --sort=-version:refname | head -5

# 최신 태그 이후 커밋 목록 (태그가 없으면 전체)
git log $(git describe --tags --abbrev=0 2>/dev/null || git rev-list --max-parents=0 HEAD)..HEAD --oneline

# CHANGELOG.md 존재 여부
ls CHANGELOG.md 2>/dev/null && echo "exists" || echo "not found"
```

$ARGUMENTS 가 있으면 해당 버전 또는 범위를 기준으로 처리하세요.
예) `/changelog v1.2.0` → v1.2.0 태그 이후 커밋 기준

## 2. 커밋 분석

커밋 메시지를 다음 규칙으로 분류하세요:

| 커밋 접두어 | CHANGELOG 섹션 |
|------------|---------------|
| `feat:` | **Added** |
| `fix:` | **Fixed** |
| `refactor:` | **Changed** |
| `perf:` | **Changed** |
| `chore:`, `build:`, `ci:` | **Changed** (중요한 것만 포함) |
| `docs:` | **Changed** (중요한 것만 포함) |
| `revert:` | **Fixed** |
| `BREAKING CHANGE` | 섹션 상단에 ⚠️ **Breaking Changes** 별도 표기 |

- Merge commit은 제외
- 의미 없는 `chore:` (lint, 오타 등)은 제외
- 각 항목은 한국어 또는 영어 원문 그대로 사용 (커밋 메시지 언어 유지)

## 3. CHANGELOG.md 업데이트

Keep a Changelog(https://keepachangelog.com) 형식을 사용하세요.

### CHANGELOG.md가 없는 경우
새 파일을 생성하세요:

```markdown
# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

### Added
- ...

### Fixed
- ...

### Changed
- ...
```

### CHANGELOG.md가 있는 경우
- 파일 상단의 `## [Unreleased]` 섹션을 찾아 업데이트
- 이미 릴리즈된 버전이 최상단이라면 새 `## [Unreleased]` 섹션을 추가
- 중복 항목은 추가하지 않음

## 4. 결과 보고

업데이트 후 다음을 출력하세요:
- 처리한 커밋 수
- 추가된 항목 수 (섹션별)
- CHANGELOG.md 경로
