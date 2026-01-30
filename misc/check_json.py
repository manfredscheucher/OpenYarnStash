import json
import argparse
import random
import os
from datetime import datetime

def process(filename):
    # Generate timestamp for the backup file
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_path = f"{filename}.original.{timestamp}"
    
    # 1. Rename original file to .original.timestamp
    try:
        os.rename(filename, backup_path)
        print(f"Backup created: {backup_path}")
    except FileNotFoundError:
        print(f"Error: The file '{filename}' was not found.")
        return
    except Exception as e:
        print(f"Error during renaming: {e}")
        return

    # 2. Load the data from the backup
    try:
        with open(backup_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except json.JSONDecodeError:
        print(f"Error: '{backup_path}' is not a valid JSON file.")
        return

    # Validate required attributes
    if "yarns" not in data:
        print("Error: Required attribute 'yarns' not found in JSON.")
        return
    if "projects" not in data:
        print("Error: Required attribute 'projects' not found in JSON.")
        return

    # Handle assignments (with backward compatibility for 'usages')
    if "assignments" in data:
        assignments = data["assignments"]
    elif "usages" in data:
        print("Info: Old attribute name 'usages' found, renaming to 'assignments'.")
        assignments = data["usages"]
        data["assignments"] = assignments
        del data["usages"]
    else:
        print("Error: Required attribute 'assignments' (or old name 'usages') not found in JSON.")
        return

    yarns = data["yarns"]
    projects = data["projects"]
    
    used_ids = set()
    new_id_count = 0
    duplicate_ids = []

    def get_matches(entity_list, key, target_id):
        return [e for e in entity_list if e.get(key) == target_id]

    # 3. Process assignments
    for a in assignments:
        # Check if ID exists, otherwise generate a unique one
        if "id" not in a:
            new_id = random.getrandbits(31)
            # Ensure the new random ID isn't already in our set
            while new_id in used_ids:
                new_id = random.getrandbits(31)
            a["id"] = new_id
            used_ids.add(new_id)
            new_id_count += 1
        else:
            current_id = a["id"]
            if current_id in used_ids:
                duplicate_ids.append(current_id)
            used_ids.add(current_id)

        # Validation: Project link
        proj_id = a.get("projectId")
        matching_projects = get_matches(projects, "id", proj_id)
        if len(matching_projects) != 1:
            print(f"Validation Warning: Assignment {a.get('id')} references {len(matching_projects)} "
                  f"projects with ID {proj_id} (Expected: 1).")

        # Validation: Yarn link
        yarn_id = a.get("yarnId")
        matching_yarns = get_matches(yarns, "id", yarn_id)
        if len(matching_yarns) != 1:
            print(f"Validation Warning: Assignment {a.get('id')} references {len(matching_yarns)} "
                  f"yarns with ID {yarn_id} (Expected: 1).")

    # 4. Summary Output
    if new_id_count > 0:
        print(f"Info: {new_id_count} assignments received a new random ID.")
    else:
        print("Info: No new IDs needed.")
    
    if duplicate_ids:
        print(f"Warning: Found {len(duplicate_ids)} duplicate IDs in the file: {duplicate_ids}")

    # 5. Write to the original filename
    try:
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=4, ensure_ascii=False)
        print(f"Success: Updated data saved to original path: {filename}")
    except Exception as e:
        print(f"Error saving file: {e}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Update craft JSON data with timestamped backup.")
    parser.add_argument("inputfile", help="Path to the JSON file to process")
    
    args = parser.parse_args()
    process(args.inputfile)