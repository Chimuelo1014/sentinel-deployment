#!/usr/bin/env python3
"""
Add logging limits to all services in docker-compose.yml
This prevents log files from consuming disk space and memory
"""

import re
import sys

def add_logging_to_service(service_block):
    """Add logging configuration to a service block if it doesn't have one"""
    
    # Check if logging already exists
    if 'logging:' in service_block:
        return service_block
    
    # Find the networks line (usually last in a service)
    lines = service_block.split('\n')
    result = []
    inserted = False
    
    for i, line in enumerate(lines):
        result.append(line)
        
        # Insert logging before networks or at the end
        if not inserted and ('networks:' in line or 'restart:' in line):
            indent = len(line) - len(line.lstrip())
            logging_config = f"""{' ' * indent}logging:
{' ' * (indent + 2)}driver: "json-file"
{' ' * (indent + 2)}options:
{' ' * (indent + 4)}max-size: "1m"
{' ' * (indent + 4)}max-file: "1"
"""
            result.insert(-1, logging_config)
            inserted = True
    
    # If we didn't insert it yet, add at the end
    if not inserted and len(result) > 1:
        # Get indentation from last non-empty line
        for line in reversed(result):
            if line.strip():
                indent = len(line) - len(line.lstrip())
                break
        
        logging_config = f"""{' ' * indent}logging:
{' ' * (indent + 2)}driver: "json-file"
{' ' * (indent + 2)}options:
{' ' * (indent + 4)}max-size: "1m"
{' ' * (indent + 4)}max-file: "1"
"""
        result.append(logging_config)
    
    return '\n'.join(result)

def main():
    input_file = 'docker-compose.yml'
    output_file = 'docker-compose.yml'
    
    print(f"📖 Reading {input_file}...")
    with open(input_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Split by services
    services_match = re.search(r'services:(.*?)(?=\nvolumes:|\nnetworks:|\Z)', content, re.DOTALL)
    if not services_match:
        print("❌ Could not find services section")
        return 1
    
    services_section = services_match.group(1)
    before_services = content[:services_match.start(1)]
    after_services = content[services_match.end(1):]
    
    # Find all service definitions
    service_pattern = r'(\n  \w+:.*?)(?=\n  \w+:|\Z)'
    services = re.findall(service_pattern, services_section, re.DOTALL)
    
    print(f"🔍 Found {len(services)} services")
    
    # Add logging to each service
    modified_services = []
    for service in services:
        modified = add_logging_to_service(service)
        modified_services.append(modified)
    
    # Reconstruct the file
    new_services_section = ''.join(modified_services)
    new_content = before_services + new_services_section + after_services
    
    # Write back
    print(f"💾 Writing to {output_file}...")
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    print("✅ Done! All services now have logging limits (1MB max, 1 file)")
    print("   This will prevent log bloat and save disk/memory")
    
    return 0

if __name__ == '__main__':
    sys.exit(main())
