export const EMAIL_CODE_COOLDOWN_SECONDS = 300;

export function formatEmailCooldown(seconds: number): string {
  const minutes = Math.floor(seconds / 60);
  const remainSeconds = seconds % 60;
  return `${minutes}:${remainSeconds.toString().padStart(2, "0")}`;
}
