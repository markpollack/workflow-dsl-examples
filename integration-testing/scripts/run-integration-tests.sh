#!/bin/bash
#
# Run all integration tests for Workflow DSL Tutorial
#
# Usage:
#   ./scripts/run-integration-tests.sh           # Run all tests
#   ./scripts/run-integration-tests.sh --quick   # Run only modules 01-03 (fastest)
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PASSED=0
FAILED=0
SKIPPED=0

# All DSL modules (require OPENAI_API_KEY)
DSL_MODULES=(
    "module-01-sequential"
    "module-02-branch"
    "module-03-error-recovery"
    "module-04-loop"
    "module-05-parallel"
    "module-06-decision"
    "module-07-gate"
    "module-08-supervisor"
)

QUICK_MODULES=(
    "module-01-sequential"
    "module-02-branch"
    "module-03-error-recovery"
)

# Parse args
MODULES=("${DSL_MODULES[@]}")
if [ "$1" == "--quick" ]; then
    MODULES=("${QUICK_MODULES[@]}")
    echo -e "${YELLOW}Running quick tests only (modules 01-03)${NC}"
fi

echo "================================================================"
echo "   Workflow DSL Tutorial - Integration Test Suite"
echo "================================================================"
echo ""

# Check for required environment
if [ -z "$OPENAI_API_KEY" ]; then
    echo -e "${RED}ERROR: OPENAI_API_KEY is not set${NC}"
    echo "All modules require OPENAI_API_KEY for real LLM calls."
    echo "Export it before running: export OPENAI_API_KEY=sk-..."
    exit 1
fi

run_test() {
    local module=$1
    echo ""
    echo "----------------------------------------------------------------"
    echo "Running: $module"
    echo "----------------------------------------------------------------"

    if jbang RunIntegrationTest.java "$module"; then
        echo -e "${GREEN}PASSED: $module${NC}"
        ((PASSED++))
    else
        echo -e "${RED}FAILED: $module${NC}"
        ((FAILED++))
    fi
}

echo "DSL Module Tests (OPENAI_API_KEY required)"
echo "-------------------------------------------"
for module in "${MODULES[@]}"; do
    if [ -f "configs/${module}.json" ]; then
        run_test "$module"
    else
        echo -e "${YELLOW}Skipping $module (no config)${NC}"
        ((SKIPPED++))
    fi
done

echo ""
echo "================================================================"
echo "   Test Summary"
echo "================================================================"
echo -e "   ${GREEN}Passed:${NC}  $PASSED"
echo -e "   ${RED}Failed:${NC}  $FAILED"
echo -e "   ${YELLOW}Skipped:${NC} $SKIPPED"
echo ""

if [ $FAILED -gt 0 ]; then
    echo -e "${RED}Some tests failed${NC}"
    exit 1
else
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
fi
