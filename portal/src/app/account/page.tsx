"use client";

import { useAuth } from "@/lib/auth";

export default function AccountPage() {
  const { user } = useAuth();

  if (!user) return null;

  return (
    <>
      <h2 className="text-xl font-medium tracking-wider uppercase mb-8">
        Account Information
      </h2>
      <div className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="text-sm text-muted block mb-1">Username</label>
            <p className="text-foreground">{user.username}</p>
          </div>
          <div>
            <label className="text-sm text-muted block mb-1">Email</label>
            <p className="text-foreground">{user.email}</p>
          </div>
          <div>
            <label className="text-sm text-muted block mb-1">WhatsApp</label>
            <p className="text-foreground">{user.whatsapp || "-"}</p>
          </div>
        </div>
      </div>
    </>
  );
}
