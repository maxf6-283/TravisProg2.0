import re
import math
import argparse
import statistics

def parse_line(line):
    # Remove comments in brackets
    line = re.sub(r'\[.*?\]', '', line)
    line = line.split(';')[0]
    # Find all letter-number pairs
    matches = re.findall(r'([A-Z])\s*([-+]?\d*\.?\d+)', line.upper())
    params = {}
    for letter, value in matches:
        try:
            params[letter] = float(value)
        except ValueError:
            pass
    return params

def calculate_distance(p1, p2):
    return math.sqrt((p1[0] - p2[0])**2 + (p1[1] - p2[1])**2)

def analyze_file(filepath):
    current_x = 0.0
    current_y = 0.0
    current_z = 0.0
    
    # Track the active G-code mode (0, 1, 2, or 3). Default to G0 (Rapid)
    active_g_code = 0 
    
    arc_errors = []
    
    print(f"Analyzing: {filepath}")
    print(f"{'Line':<6} | {'Type':<4} | {'Start Radius':<12} | {'End Radius':<12} | {'Error (Diff)':<12}")
    print("-" * 65)

    with open(filepath, 'r') as f:
        for i, line in enumerate(f, 1):
            original_line = line.strip()
            if not original_line:
                continue
                
            params = parse_line(original_line)
            
            # If line has no commands (e.g. comment only), skip
            if not params:
                continue

            # Update Active G-Code if present
            if 'G' in params:
                g_val = int(params['G'])
                # Only update mode for motion commands
                if g_val in [0, 1, 2, 3]:
                    active_g_code = g_val

            # Determine Target Positions (Defaults to current if missing)
            end_x = params.get('X', current_x)
            end_y = params.get('Y', current_y)
            end_z = params.get('Z', current_z)

            # --- Logic for G0 / G1 (Linear) ---
            if active_g_code in [0, 1]:
                # Just update position
                current_x = end_x
                current_y = end_y
                current_z = end_z

            # --- Logic for G2 / G3 (Arc) ---
            elif active_g_code in [2, 3]:
                # If the line is purely a Z move (Helical) without X/Y movement, 
                # strictly speaking, it's a helix. WinCNC might treat it as a line or arc.
                # We will calculate arc error only if X or Y or I or J is present.
                
                has_arc_params = any(k in params for k in ['X', 'Y', 'I', 'J'])
                
                if has_arc_params:
                    # Offsets (WinCNC I/J are relative to Start Point)
                    i_offset = params.get('I', 0.0)
                    j_offset = params.get('J', 0.0)
                    
                    # Calculate Center
                    center_x = current_x + i_offset
                    center_y = current_y + j_offset
                    
                    # Radius 1: Distance from Center to Start
                    radius_start = calculate_distance((center_x, center_y), (current_x, current_y))
                    
                    # Radius 2: Distance from Center to End
                    radius_end = calculate_distance((center_x, center_y), (end_x, end_y))
                    
                    # Error Calculation
                    error = abs(radius_start - radius_end)
                    arc_errors.append(error)
                    
                    # Print Detail
                    print(f"{i:<6} | G{active_g_code:<3} | {radius_start:<12.6f} | {radius_end:<12.6f} | {error:<12.6f}")

                # Update Position
                current_x = end_x
                current_y = end_y
                current_z = end_z

    print("-" * 65)
    
    if arc_errors:
        max_error = max(arc_errors)
        avg_error = statistics.mean(arc_errors)
        FAIL_THRESHOLD = 0.05
        failed_arcs = sum(1 for e in arc_errors if e > FAIL_THRESHOLD)

        print("\n--- Statistics ---")
        print(f"Total Arcs Processed: {len(arc_errors)}")
        print(f"Maximum Error:        {max_error:.6f}")
        print(f"Average Error:        {avg_error:.6f}")
        print(f"Arcs exceeding {FAIL_THRESHOLD}:      {failed_arcs}")
    else:
        print("No arcs found.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("file")
    args = parser.parse_args()
    analyze_file(args.file)
