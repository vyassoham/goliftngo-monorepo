import subprocess
import os
import shutil

def sign_bundle():
    keystore = r"D:\Projects\liftngo-upload-key.jks"
    if not os.path.exists(keystore):
        keystore = r"D:\Projects\goliftngo-driver\liftngo-upload-key.jks"

    alias = "liftngo-upload"
    password = "LiftngoUploadKey2026Secure!"
    
    input_aab = r"D:\Projects\goliftngo-driver\app\build\outputs\bundle\debug\app-debug.aab"
    output_aab = r"D:\Projects\goliftngo-driver\LiftnGo_Delivery_Partner_Signed_v1.aab"
    desktop_aab = os.path.join(os.path.expanduser('~/Desktop'), 'LiftnGo_Delivery_Partner_Signed_v1.aab')
    
    shutil.copy(input_aab, output_aab)
    
    cmd = [
        "jarsigner",
        "-verbose",
        "-sigalg", "SHA256withRSA",
        "-digestalg", "SHA-256",
        "-keystore", keystore,
        "-storepass", password,
        "-keypass", password,
        output_aab,
        alias
    ]
    
    print("Signing AAB with jarsigner...")
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode == 0:
        print("AAB Signed Successfully!")
        # Copy to desktop
        shutil.copy(output_aab, desktop_aab)
        print(f"Copied signed AAB to Desktop: {desktop_aab}")
    else:
        print("Jarsigner failed:", res.stderr)

    # Verify signature
    v_cmd = ["keytool", "-printcert", "-jarfile", output_aab]
    v_res = subprocess.run(v_cmd, capture_output=True, text=True)
    print("Verification Certificate SHA1:")
    print(v_res.stdout)

if __name__ == '__main__':
    sign_bundle()
