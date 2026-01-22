#!/bin/bash
# Nexis Node Manager Script
# Simplifies running and managing Nexis nodes

set -e

NEXIS_JAR="target/nexis-core-0.1.0-SNAPSHOT.jar"
NEXIS_MAIN="com.nexis.app.NexisNode"
LOG_DIR="logs"
PID_FILE="nexis.pid"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_banner() {
    echo -e "${BLUE}"
    echo "╔═══════════════════════════════════════╗"
    echo "║        NEXIS BLOCKCHAIN NODE          ║"
    echo "║         Management Script             ║"
    echo "╚═══════════════════════════════════════╝"
    echo -e "${NC}"
}

check_jar() {
    if [ ! -f "$NEXIS_JAR" ]; then
        echo -e "${RED}Error: JAR file not found at $NEXIS_JAR${NC}"
        echo "Please run: mvn clean package"
        exit 1
    fi
}

start_node() {
    print_banner
    check_jar
    
    echo -e "${GREEN}Starting Nexis node...${NC}"
    
    # Create logs directory
    mkdir -p "$LOG_DIR"
    
    # Start node
    java -Xmx4G -Xms2G \
        -XX:+UseG1GC \
        -XX:MaxGCPauseMillis=200 \
        -Dfile.encoding=UTF-8 \
        -cp "$NEXIS_JAR" "$NEXIS_MAIN" 2>&1 | tee "$LOG_DIR/nexis-$(date +%Y%m%d-%H%M%S).log"
}

start_background() {
    print_banner
    check_jar
    
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            echo -e "${YELLOW}Node already running with PID $PID${NC}"
            exit 1
        fi
    fi
    
    echo -e "${GREEN}Starting Nexis node in background...${NC}"
    mkdir -p "$LOG_DIR"
    
    nohup java -Xmx4G -Xms2G \
        -XX:+UseG1GC \
        -XX:MaxGCPauseMillis=200 \
        -Dfile.encoding=UTF-8 \
        -cp "$NEXIS_JAR" "$NEXIS_MAIN" \
        > "$LOG_DIR/nexis.log" 2>&1 &
    
    echo $! > "$PID_FILE"
    echo -e "${GREEN}Node started with PID $(cat $PID_FILE)${NC}"
    echo -e "${BLUE}Dashboard: http://localhost:8000${NC}"
    echo -e "View logs: tail -f $LOG_DIR/nexis.log"
}

stop_node() {
    if [ ! -f "$PID_FILE" ]; then
        echo -e "${RED}No PID file found. Node may not be running.${NC}"
        exit 1
    fi
    
    PID=$(cat "$PID_FILE")
    
    if ps -p "$PID" > /dev/null 2>&1; then
        echo -e "${YELLOW}Stopping Nexis node (PID $PID)...${NC}"
        kill "$PID"
        rm "$PID_FILE"
        echo -e "${GREEN}Node stopped successfully${NC}"
    else
        echo -e "${RED}Process $PID not found${NC}"
        rm "$PID_FILE"
    fi
}

status_node() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ Node is RUNNING (PID $PID)${NC}"
            echo -e "${BLUE}Dashboard: http://localhost:8000${NC}"
            
            # Check if API is responsive
            if command -v curl > /dev/null; then
                if curl -s http://localhost:8000/api/stats > /dev/null; then
                    echo -e "${GREEN}✓ API is RESPONSIVE${NC}"
                else
                    echo -e "${YELLOW}⚠ API not responding yet${NC}"
                fi
            fi
        else
            echo -e "${RED}✗ Node is NOT RUNNING (stale PID file)${NC}"
            rm "$PID_FILE"
        fi
    else
        echo -e "${RED}✗ Node is NOT RUNNING${NC}"
    fi
}

logs_node() {
    if [ -f "$LOG_DIR/nexis.log" ]; then
        tail -f "$LOG_DIR/nexis.log"
    else
        echo -e "${RED}No log file found${NC}"
        exit 1
    fi
}

build_node() {
    print_banner
    echo -e "${GREEN}Building Nexis...${NC}"
    mvn clean package -DskipTests
    echo -e "${GREEN}Build complete!${NC}"
}

test_node() {
    print_banner
    echo -e "${GREEN}Running tests...${NC}"
    mvn test
}

help_menu() {
    print_banner
    echo "Usage: $0 {command}"
    echo ""
    echo "Commands:"
    echo "  start       - Start node in foreground (interactive)"
    echo "  background  - Start node in background (daemon)"
    echo "  stop        - Stop background node"
    echo "  status      - Check node status"
    echo "  logs        - Tail log file"
    echo "  build       - Build the project"
    echo "  test        - Run tests"
    echo "  restart     - Restart background node"
    echo "  help        - Show this help"
    echo ""
}

case "$1" in
    start)
        start_node
        ;;
    background|daemon)
        start_background
        ;;
    stop)
        stop_node
        ;;
    status)
        status_node
        ;;
    logs)
        logs_node
        ;;
    build)
        build_node
        ;;
    test)
        test_node
        ;;
    restart)
        stop_node
        sleep 2
        start_background
        ;;
    help|--help|-h)
        help_menu
        ;;
    *)
        help_menu
        exit 1
        ;;
esac

exit 0
