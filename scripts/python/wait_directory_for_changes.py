import argparse
import os
import json
# Firebase imports
from firebase import firebase
import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
# Windows specific imports
import win32file
import win32con

CREATE = 1
DELETE = 2
UPDATE = 3

# File actions
ACTIONS = {
  1 : "Created",
  2 : "Deleted",
  3 : "Updated",
  4 : "Renamed from something",
  5 : "Renamed to something"
}

FILE_LIST_DIRECTORY = 0x0001

def post_to_firebase(file_path, stats_ref):
    with open(file_path) as json_file:  
        data = json.load(json_file)
        stats_ref.set(data)

def setup_firebase():
    # Initialize the app with a service account, granting admin privileges
    cred = credentials.Certificate('serviceAccountKey.json')
    firebase_admin.initialize_app(cred, {
        'databaseURL': 'https://factorio-companion.firebaseio.com/'
    })

def monitor_directory(path, firebase_items_ref):
    stats_ref = firebase_items_ref.child('items')

    hDir = win32file.CreateFile (
        path,
        FILE_LIST_DIRECTORY,
        win32con.FILE_SHARE_READ | win32con.FILE_SHARE_WRITE | win32con.FILE_SHARE_DELETE,
        None,
        win32con.OPEN_EXISTING,
        win32con.FILE_FLAG_BACKUP_SEMANTICS,
        None
    )
    while 1:
        #
        # ReadDirectoryChangesW takes a previously-created
        # handle to a directory, a buffer size for results,
        # a flag to indicate whether to watch subtrees and
        # a filter of what changes to notify.
        #
        # NB Tim Juchcinski reports that he needed to up
        # the buffer size to be sure of picking up all
        # events when a large number of files were
        # deleted at once.
        #
        results = win32file.ReadDirectoryChangesW (
            hDir,
            1024,
            True,
            win32con.FILE_NOTIFY_CHANGE_FILE_NAME |
            win32con.FILE_NOTIFY_CHANGE_DIR_NAME |
            win32con.FILE_NOTIFY_CHANGE_ATTRIBUTES |
            win32con.FILE_NOTIFY_CHANGE_SIZE |
            win32con.FILE_NOTIFY_CHANGE_LAST_WRITE |
            win32con.FILE_NOTIFY_CHANGE_SECURITY,
            None,
            None
        )
        for action, file in results:
            full_filename = os.path.join (path, file)
            if ACTIONS.get (action, "Unknown") == ACTIONS[UPDATE]:
                print(full_filename, ACTIONS.get (action, "Unknown")) 
                # Post content updates to firebase
                post_to_firebase(full_filename, stats_ref)

def main():
    parser = argparse.ArgumentParser(
        description=
        """
        Monitors a directory and uploads file changes to firebase.
        """
    )
    parser.add_argument("path", help="path to target directory")
    args = parser.parse_args()
    print("Selected path: ", args.path)

    # Setup Firebase
    setup_firebase()

    # Get stats db ref
    stats_ref = db.reference('stats').child('items')

    # Monitor directory
    monitor_directory(args.path, stats_ref)

if __name__== "__main__":
  main()